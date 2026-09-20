# Cover Widget Container

Hosts home-screen widgets and draws them inside cover-screen widgets of the Samsung Galaxy Z Flip.

## How it works
- 20 cover-screen widgets ("Container 1" … "Container 20") are declared with `display="sub_screen"`.
- A cover widget is a `RemoteViews`, which cannot embed another widget. Each hosted widget is therefore rendered to a bitmap
  (`WidgetSnapshotRenderer`) and pushed into the container's `ImageView` (`ContainerRenderer`).
- `SnapshotService` (foreground) keeps the `AppWidgetHost` listening and re-renders the containers whenever a hosted widget updates.
- Containers with several pages show ‹ › buttons to change page.
- The main-screen app manages the widget library and the content (pages, order) of each container.

## Limits
- Widgets are images: they refresh live, but taps are not forwarded to them.
- Widgets built on list adapters (collections) may render empty.
- A persistent (minimal) notification is required for the refresh service. After a reboot it restarts through `BootReceiver`.

## Build and install
```
JAVA_HOME=<JDK 17+> ./gradlew assembleDebug
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Setup on the phone
1. Open the app, allow notifications, add widgets to the library, assign them to containers.
2. Settings › Cover screen › Widgets: add "Container N" widgets.

## Regenerating the 20 providers
`scripts/gen_providers.sh` rewrites `ContainerProviders.kt`, `container_labels.xml` and the receivers block of the manifest.
