# Coding on Pad

A lightweight Android news reader for the AI, ML research, and coding world — built to
run locally on an Android tablet and pull fresh items from curated RSS/Atom feeds
every time you open it.

## Features

- Native Android (Kotlin + Jetpack Compose, Material 3)
- Curated sources: arXiv (cs.AI / cs.LG / cs.CL), Hacker News, Simon Willison,
  The Batch, Anthropic, OpenAI, Google AI, GitHub Blog, GitHub Trending, MIT Tech Review
- Category filters: Research / AI News / Coding / HN
- Offline HTTP cache (10 MB) + dedupe + sort by publish time
- No accounts, no API keys, no tracking — just RSS
- Tap an item to open the source in your browser

## Customize feeds

Edit `app/src/main/java/dev/codingonpad/news/data/FeedSource.kt` — add or remove entries
in `FeedSource.ALL` and rebuild.

## Install on your tablet

### Option A: grab the APK from GitHub Actions

1. Push a commit (or open the workflow manually from the Actions tab).
2. Open the latest **Build APK** run in GitHub Actions.
3. Download the `CodingOnPad-debug` artifact (contains a `.apk`).
4. Copy the APK to the tablet and tap it to install. Enable
   *Install unknown apps* for your file manager the first time.

### Option B: build locally

Requires JDK 17 and the Android SDK (API 34). From the repo root:

```sh
gradle wrapper --gradle-version 8.10.2
./gradlew :app:assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

## Minimum Android version

Android 8.0 (API 26) — covers essentially every tablet in use today.

## Notes

- The debug APK is signed with the Android debug keystore, which is fine for personal
  sideloading. For a signed release build, wire up a keystore in `app/build.gradle.kts`.
- RSS feed URLs can change over time; if a source stops returning items, check its site
  and update the URL in `FeedSource.kt`.
