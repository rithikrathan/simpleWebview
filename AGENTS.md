# AGENTS.md

Guidance for AI agents and contributors working in this repository.

## Project overview

An Android app that acts as a mini launcher for web pages: a home screen with
saved-page shortcuts (with website favicons) plus a browser-style URL bar, and a
fullscreen WebView page for the currently open site.

- `MainActivity` — home screen. Edge-to-edge, Material 3. A URL bar (browser-like,
  with a **+** button that saves the current URL as a shortcut) above a 3-column
  tiled grid of saved pages. Typing a URL opens it one-time; tapping a tile opens
  that page; long-pressing a tile deletes it. Shows the last-viewed URL when you
  return from a page.
- `WebViewActivity` — fullscreen immersive WebView. Left-edge swipe (rightward)
  and the hardware back button (after exhausting page history) return to Home,
  passing the current page URL back so the home URL bar stays in sync.

This is **not** a kiosk-blocking app. The WebView intentionally does **not** block
web features (JavaScript, notifications, geolocation, camera/mic permissions,
JS dialogs).

## Build

Building happens **only** in GitHub Actions. Do not install Android SDK components
or Gradle locally.

- Debug APK: `./gradlew assembleDebug`
- CI artifacts: `.github/workflows/build.yml` uploads `app-debug.apk` on every push
  to `master`.
- Releases: pushing a tag like `v1.0.0` triggers `.github/workflows/release.yml`,
  which builds, signs and publishes `app-release.apk` to a GitHub Release.
- Signing: release builds are signed via the GitHub secrets `KEYSTORE_BASE64`,
  `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` (see `signingConfigs.release`
  in `app/build.gradle.kts`). A local backup of the keystore lives in the
  **gitignored** `keystore/` folder — never commit it, and never hardcode the
  passwords in source.

Versions: Gradle 8.7 (wrapper), AGP 8.5.2, Kotlin 1.9.24, JDK 17,
compileSdk/targetSdk 34, minSdk 24.

## Structure

```
app/src/main/
  AndroidManifest.xml
  java/dev/rithikrathan/simplewebview/
    MainActivity.kt          (home: URL bar + shortcut grid)
    WebViewActivity.kt       (fullscreen WebView page)
    ShortcutRepository.kt    (SharedPreferences JSON persistence of saved pages)
    ShortcutAdapter.kt       (grid adapter; Coil favicons + letter fallback)
    UrlUtil.kt               (URL normalization + host helpers)
  res/layout/activity_main.xml
  res/layout/activity_webview.xml
  res/layout/item_shortcut.xml
  res/drawable/ (icons + adaptive launcher icon)
  res/values/ + res/values-night/ (strings, colors, themes — Material 3)
keystore/ -> gitignored local backup of the release keystore (never commit)
.gradle / build / local.properties -> gitignored (never commit)
.github/workflows/build.yml
.github/workflows/release.yml
gradlew, gradlew.bat, gradle/wrapper/  (committed, required for CI)
```

## Hard invariants (do not break)

1. **One page at a time.** `WebViewActivity` is a single, replaceable session.
   Opening any URL (icon or URL bar) from Home starts a fresh page; the previous
   page is destroyed when you leave it.
2. **No cookies / no persisted browsing state.** Cookies, DOM storage and cache are
   cleared before each page loads and on app launch. `setSaveFormData(false)` and
   `setSavePassword(false)` are set. The **only** persisted data is the user's
   saved-shortcut list (`ShortcutRepository`, SharedPreferences).
3. **The one-time/current URL is memory-only.** It is never written to disk. The
   home URL bar reflects the last page URL only in memory, via the
   `RESULT_OK` + `EXTRA_URL` result passed back from `WebViewActivity`.
4. **Back = page history, then Home.** In `WebViewActivity`, the
   `OnBackPressedCallback` calls `webView.goBack()` when `webView.canGoBack()`,
   otherwise it returns to Home (exits only from Home). Left-edge rightward swipe
   also returns Home.
5. **Web features are not blocked.** JavaScript is enabled, all `onPermissionRequest`
   grants are accepted, and `target=_blank` links stay inside the same WebView
   (`setSupportMultipleWindows(false)`).

## Conventions

- Kotlin, view binding (`ActivityMainBinding`, `ActivityWebviewBinding`,
  `ItemShortcutBinding`).
- Material 3 (`Theme.SimpleWebview`), favicons via Coil, shortcuts via
  SharedPreferences JSON (org.json).
- No code comments unless necessary; keep the code minimal and dependency-light.
