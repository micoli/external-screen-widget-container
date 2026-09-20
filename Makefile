# Debug helpers for Cover Widget Container. Run `make` to list the targets.
PACKAGE   := org.micoli.coverwidgetcontainer
MAIN_ACTIVITY := $(PACKAGE)/.ui.ConfigActivity
A11Y_SERVICE  := $(PACKAGE)/$(PACKAGE).overlay.CoverOverlayService
APK       := app/build/outputs/apk/debug/app-debug.apk
GRADLE    := ./gradlew

# adb lookup: PATH, then ANDROID_HOME, then the default macOS SDK location.
ADB ?= $(or $(shell command -v adb),$(if $(ANDROID_HOME),$(ANDROID_HOME)/platform-tools/adb),$(HOME)/Library/Android/sdk/platform-tools/adb)
# Set SERIAL (for example 192.168.2.92:40247) when several devices are connected.
SERIAL ?=
ADB_S  := $(ADB) $(if $(SERIAL),-s $(SERIAL))

# Wireless debugging: HOST is the phone IP, PORT its connect port, PAIR_PORT/CODE come from the pairing dialog.
HOST ?= 192.168.2.92
PORT ?=
PAIR_PORT ?=
CODE ?=

.DEFAULT_GOAL := help
.NOTPARALLEL:
.PHONY: help build test clean release-tag deploy install launch stop uninstall \
	adb-devices adb-mdns adb-pair adb-connect adb-disconnect adb-restart \
	logs logs-all logs-clear a11y-status fold-state widgets-placed app-data cover-dump

help: ## List the targets
	@awk 'BEGIN {FS = ":.*## "} /^[a-zA-Z0-9_-]+:.*## / {printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@echo
	@echo "Variables: HOST=$(HOST) PORT=<port> PAIR_PORT=<port> CODE=<code> SERIAL=<serial> ADB=$(ADB)"

## Build

build: ## Build the debug APK
	$(GRADLE) assembleDebug --console=plain

test: ## Run the unit tests
	$(GRADLE) testDebugUnitTest --console=plain

clean: ## Remove build outputs
	$(GRADLE) clean --console=plain

## Release

VERSION ?=

release-tag: ## Create the local tag v$(VERSION); push it to publish a release: make release-tag VERSION=0.2.0
	@test -n "$(VERSION)" || { echo "usage: make release-tag VERSION=<x.y.z>"; exit 1; }
	@test -z "$$(git status --porcelain)" || { echo "working tree not clean, commit first"; exit 1; }
	git tag -a v$(VERSION) -m "Release $(VERSION)"
	@echo "Tag v$(VERSION) created. Publish it with: git push origin v$(VERSION)"

## Device

deploy: build install launch ## Build, install and start the debug app
	@echo "Reinstalling disables the accessibility service: re-enable it in Settings > Accessibility."

install: ## Install the debug APK (keeps app data)
	@test -f $(APK) || { echo "$(APK) missing, run 'make build' first"; exit 1; }
	$(ADB_S) install -r $(APK)

launch: ## Start the configuration app
	$(ADB_S) shell am start -n $(MAIN_ACTIVITY)

stop: ## Force-stop the app
	$(ADB_S) shell am force-stop $(PACKAGE)

uninstall: ## Uninstall the app and its data
	$(ADB_S) uninstall $(PACKAGE)

## adb

adb-devices: ## List connected devices
	$(ADB) devices -l

adb-mdns: ## List wireless debugging services found on the network
	$(ADB) mdns services

adb-pair: ## Pair over Wi-Fi: make adb-pair PAIR_PORT=38167 CODE=123456
	@test -n "$(PAIR_PORT)" -a -n "$(CODE)" || { echo "usage: make adb-pair PAIR_PORT=<port> CODE=<code> [HOST=$(HOST)]"; exit 1; }
	$(ADB) pair $(HOST):$(PAIR_PORT) $(CODE)

adb-connect: ## Connect over Wi-Fi: make adb-connect PORT=40247
	@test -n "$(PORT)" || { echo "usage: make adb-connect PORT=<port> [HOST=$(HOST)]"; exit 1; }
	$(ADB) connect $(HOST):$(PORT)

adb-disconnect: ## Disconnect every wireless device
	$(ADB) disconnect

adb-restart: ## Restart the adb server
	$(ADB) kill-server
	$(ADB) start-server

## Logs and state

logs: ## Follow the overlay service logs and crashes
	$(ADB_S) logcat -v time CoverOverlay:D HostedWidgetManager:I AndroidRuntime:E '*:S'

logs-all: ## Follow every log line of the app process
	$(ADB_S) logcat -v time --pid=$$($(ADB_S) shell pidof $(PACKAGE))

logs-clear: ## Clear the device log buffer
	$(ADB_S) logcat -c

a11y-status: ## Show whether the overlay accessibility service is enabled
	@echo "enabled services:"
	@$(ADB_S) shell settings get secure enabled_accessibility_services | tr ':' '\n' | sed 's/^/  /'
	@$(ADB_S) shell settings get secure enabled_accessibility_services | grep -q "$(A11Y_SERVICE)" \
	    && echo "=> overlay service ENABLED" || echo "=> overlay service NOT enabled"

fold-state: ## Show the fold state (OPENED or CLOSED)
	@$(ADB_S) shell dumpsys device_state | grep -E "mBaseState|mCommittedState" | grep -o "name='[A-Z_]*'" | sort -u

widgets-placed: ## Show the Container widgets placed on the cover screen (id, host, provider)
	@$(ADB_S) shell dumpsys appwidget | awk '/^Widgets:/{f=1;next} /^Hosts:/{f=0} f' \
		| awk '/^  \[[0-9]+\] id=/{if (b ~ /Container[0-9][0-9]Provider/) print b; b=$$0; next} {b=b"\n"$$0} END{if (b ~ /Container[0-9][0-9]Provider/) print b}' \
		| grep -E "id=|host=|provider=" \
		| sed -E 's/ProviderId\{[^}]*cmp:ComponentInfo\{[^/]*\///; s/\}\}$$//; s/HostId\{[^}]*pkg://; s/\}$$//'

app-data: ## Print the saved containers and widget library (debug build only)
	@$(ADB_S) shell "run-as $(PACKAGE) cat files/datastore/cover_widget_container.preferences_pb" | strings

cover-dump: ## Dump the cover screen UI tree to build/cover.xml (cover must be on and unlocked)
	@mkdir -p build
	$(ADB_S) shell "uiautomator dump --display 1 /sdcard/cover.xml"
	$(ADB_S) pull /sdcard/cover.xml build/cover.xml
