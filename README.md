# Cover Widget Container

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

## Regenerating the 20 providers
`scripts/gen_providers.sh` rewrites `ContainerProviders.kt`, `container_labels.xml` and the receivers block of the manifest.
