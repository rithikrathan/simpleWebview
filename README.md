# simpleWebview

A lightweight Android app that turns your phone into a focused fullscreen web
browser. A home screen with saved-page shortcuts (with website favicons) and a
browser-style URL bar opens sites in a fullscreen, fully-functional WebView.

## Features

- **Home launcher** — a 3-column grid of your saved pages with favicons pulled
  from each site. Tap a tile to open it, long-press to delete it.
- **Browser-style URL bar** — type any URL to open a page once (nothing is
  saved), or tap the **+** icon to save the current URL as a shortcut tile.
- **URL sync** — the home URL bar always shows the last page you visited.
- **Fullscreen pages** — pages open edge-to-edge with all web features enabled
  (JavaScript, notifications, geolocation, camera/mic permissions).
- **One page at a time** — only one browsing session exists; opening a new page
  destroys the previous one.
- **Private by design** — cookies, DOM storage and cache are cleared before each
  page loads and at app launch. The only thing stored on the device is your
  saved-shortcut list.
- **Swipe back** — swipe from the left edge (or use the back button) to return
  to the Home screen; back button walks page history first.

## Privacy

- The one-time/current URL is kept only in memory and never written to disk.
- No cookies, history, form data or saved passwords are persisted.
- The only persisted data is your saved-shortcut list, stored locally in the
  app's SharedPreferences.

## Install

Download the latest signed `app-release.apk` from the
[Releases](https://github.com/rithikrathan/simpleWebview/releases) page and
sideload it (enable "Install unknown apps" for your file manager or browser if
prompted).

> The APK is signed with the project's release key; future updates must be
> signed with the same key.

## Build

Building runs only in GitHub Actions — no local Android SDK needed.

- Every push to `master` builds the debug APK
  (`.github/workflows/build.yml`).
- Pushing a tag like `v2.1.0` triggers `.github/workflows/release.yml`, which
  builds, signs and publishes the release APK to GitHub Releases.
- Signing credentials are stored as GitHub secrets
  (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`).

Requires JDK 17. Stack: Kotlin, AGP 8.5.2, Gradle 8.7 (wrapper), Material 3,
Coil, compileSdk/targetSdk 34, minSdk 24.

## License

[MIT](LICENSE)
