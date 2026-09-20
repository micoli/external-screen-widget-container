# Cover Widget Container

[![Build](https://github.com/micoli/external-screen-widget-container/actions/workflows/build.yml/badge.svg)](https://github.com/micoli/external-screen-widget-container/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/micoli/external-screen-widget-container?include_prereleases)](https://github.com/micoli/external-screen-widget-container/releases)
![Android 14+](https://img.shields.io/badge/Android-14%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Galaxy Z Flip](https://img.shields.io/badge/Samsung-Galaxy%20Z%20Flip-1428A0?logo=samsung&logoColor=white)

Hosts home-screen widgets and draws them, live and interactive, on the Samsung Galaxy Z Flip cover screen.

## How it works
- 20 cover-screen widgets ("Container 1" … "Container 20") are declared with `display="sub_screen"`. They draw nothing:
  they only carry a marker (`cw-container-NN`) so they can be found on the cover launcher.
- `CoverOverlayService` (accessibility service, required) reads the cover launcher tree, finds each placed Container widget
  (by its marker or by the host view label) and draws the hosted widgets in an overlay window on top of it.
- Widgets are hosted by an `AppWidgetHost` in this app, so clicks, checkboxes and lists work.
- Containers with several pages get a ‹ n/m › bar in the overlay.
- The main-screen app manages the widget library and the content (pages, order) of each container.
- No foreground service and no notification: the accessibility service keeps the process alive.

## Setup
1. Open the app, add widgets to the library and assign them to containers.
2. Settings › Cover screen › Widgets: add "Container N" widgets.
3. Settings › Accessibility › Installed apps: enable "Cover Widget Container overlay".
   On sideloaded builds, allow restricted settings first (app info › ⋮).

## Limits
- A swipe starting on a hosted widget goes to that widget; change launcher page by swiping from a margin.
- The overlay follows the current Samsung cover launcher (`com.android.systemui` / aodservice). A One UI update can change it.
- Only works while the cover screen is unlocked and showing the page with the Container widget.
- Without the accessibility service a Container widget only shows its name.

## Build and install
```
JAVA_HOME=<JDK 17+> ./gradlew assembleDebug
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Makefile
`make` lists the targets. The useful ones:
- `make deploy`: build, install and start the debug app (reinstalling disables the accessibility service, re-enable it).
- `make adb-pair PAIR_PORT=… CODE=…`, `make adb-connect PORT=…`, `make adb-devices`: wireless debugging (`HOST` defaults to the phone IP).
- `make logs`, `make a11y-status`, `make widgets-placed`, `make fold-state`, `make cover-dump`: inspect the phone.
- `make release-tag VERSION=0.2.0`: create the release tag locally.

## CI and releases
`.github/workflows/build.yml` runs the unit tests and builds the debug APK on every push to `main` and on pull requests.
Download the APK from the run's artifacts (`cover-widget-container-debug-apk`).

Pushing a tag `vX.Y.Z` publishes a GitHub release with the APK attached and generated notes:
```
make release-tag VERSION=0.2.0
git push origin v0.2.0
```
The tag sets the APK version name (the CI run number is the version code). A tag with a dash (`v0.2.0-rc1`) is a pre-release.
The APK is debug-signed: it installs by sideloading, not from a store.

## Regenerating the 20 providers
`scripts/gen_providers.sh` rewrites `ContainerProviders.kt`, `container_labels.xml` and the receivers block of the manifest.
