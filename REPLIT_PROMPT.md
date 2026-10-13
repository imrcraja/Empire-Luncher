# Prompt for Replit Agent (copy-paste this)

You are a senior Android (Kotlin/Jetpack Compose) engineer. This zip is "Empire Launcher" by RC Empire,
a Minecraft: Java Edition launcher for Android (package com.rcempire.empirelauncher, minSdk 26, Hilt, Retrofit, Compose).
It is a working scaffold. Read README.md and runtime/README.md first.

TASK
1. Make the project build: install JDK 17 + Android SDK (compileSdk 35), run `gradle wrapper --gradle-version 8.9`,
   then `./gradlew :app:assembleDebug`. Fix any compile errors without changing the architecture.
2. Give me the debug APK (app/build/outputs/apk/debug/app-debug.apk).
3. Then implement, one at a time, in this order (ask before major architecture changes):
   a. Native runtime: integrate ZalithLauncher 2 (GPL-3.0) JRE + JNI + renderers so PLAY actually starts Minecraft.
   b. Room DB instances (version, loader, RAM, renderer, Java, jvmArgs) + instance create/edit UI.
   c. Version installer (client jar, libraries, assets) via Downloader (resume + SHA-1), official Mojang URLs only.
   d. Mod store: install, dependency resolution, update check, CurseForge (key from BuildConfig), .mrpack import/export.
   e. Controls editor (drag-drop, tap/hold/toggle/joystick/swipe/macro, gamepad, gyro), Fabric/Quilt/Forge/NeoForge installers.
   f. Smart Optimize wiring, performance overlay, Ely.by/authlib-injector, Hindi + Hinglish strings, splash, auto-update.

RULES
- Keep dark violet-blue Material 3 theme and the logo (res/drawable-nodpi/rc_logo.png).
- No bundled Minecraft assets; show non-affiliation + EULA note; credit open-source libs in About.
- Keep GPL-3.0 and credit ZalithLauncher/PojavLauncher for any derived code.
- Never commit API keys; use gradle properties / secrets (MS_CLIENT_ID, CURSEFORGE_API_KEY).
