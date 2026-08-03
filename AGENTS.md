# AGENTS.md

Guidance for AI agents and contributors working in this repository.

## Project overview

A minimal Android app that displays a user-entered webpage in a fullscreen WebView.
Two activities:

- `MainActivity` — a URL input screen. Normalizes the URL (adds `https://` if no
  scheme is present), launches `WebViewActivity`, then calls `finish()`.
- `WebViewActivity` — a fullscreen WebView that loads the URL passed via the
  `EXTRA_URL` intent extra.

This is **not** a kiosk-blocking app. The WebView intentionally does **not** block
web features (JavaScript, DOM storage, notifications, geolocation, camera/mic
permissions, JS dialogs).

## Build

Building happens **only** in GitHub Actions (see `.github/workflows/build.yml`).
Do not install Android SDK components or Gradle locally.

- Debug APK: `./gradlew assembleDebug` (CI)
- Artifact: `app-debug.apk`, uploaded as a GitHub Actions artifact.

Versions: Gradle 8.7 (wrapper), AGP 8.5.2, Kotlin 1.9.24, JDK 17,
compileSdk/targetSdk 34, minSdk 24.

## Structure

```
app/src/main/
  AndroidManifest.xml
  java/dev/rithikrathan/simplewebview/
    MainActivity.kt
    WebViewActivity.kt
  res/layout/activity_main.xml
  res/layout/activity_webview.xml
  res/values/ (strings, colors, themes)
  res/drawable/ic_launcher.xml
.gradle / build / local.properties -> gitignored (never commit)
.github/workflows/build.yml
gradlew, gradlew.bat, gradle/wrapper/  (committed, required for CI)
```

## Hard invariants (do not break)

1. **No way back to the URL page.** `MainActivity.finish()` after launching
   `WebViewActivity`. Never add a navigation path from the WebView screen back to
   the URL input screen.
2. **The URL is never persisted.** No `SharedPreferences`, no `savedInstanceState`,
   no files, no `onSaveInstanceState` overrides. The URL lives only in the in-memory
   intent extra.
3. **Back button = page history.** In `WebViewActivity`, the `OnBackPressedCallback`
   calls `webView.goBack()` when `webView.canGoBack()`, otherwise `finish()` (which
   exits the app, since `WebViewActivity` is the task root).
4. **Web features are not blocked.** JavaScript and DOM storage are enabled, all
   `onPermissionRequest` grants are accepted, and `target=_blank` links stay inside
   the same WebView (`setSupportMultipleWindows(false)`).

## Conventions

- Kotlin, view binding (`ActivityMainBinding`, `ActivityWebviewBinding`).
- No code comments unless necessary; keep the code minimal and dependency-light.
