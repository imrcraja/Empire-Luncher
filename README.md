# Empire Launcher (by RC Empire)
Minecraft: Java Edition launcher for Android. Status: **scaffold v0.1** - UI + accounts + store + download
core are real code; the native game runtime (JRE/JNI/renderers) is still TODO (see runtime/README.md).

## Build
Needs JDK 17 + Android SDK (compileSdk 35).
```
gradle wrapper --gradle-version 8.9      # one time, creates ./gradlew
./gradlew :app:assembleDebug -PMS_CLIENT_ID=xxxx -PCURSEFORGE_API_KEY=xxxx
```
APK: `app/build/outputs/apk/debug/app-debug.apk`. Or push to GitHub: Actions builds the APK for you.

## Done
Compose dark violet-blue UI, 5 tabs + controls/logs screens, Modrinth search, Mojang version manifest,
offline accounts, Microsoft device-code login (needs Azure client id), encrypted token store,
resumable SHA-1 downloader, Java auto-select, Smart Optimize, renderer advisor, crash analyzer, CI.

## TODO (next steps, in order)
1. Native runtime from ZalithLauncher (runtime/README.md)  2. Room DB for instances  3. Version install
(libraries/assets) 4. Mod install + dependency resolver + CurseForge  5. Controls editor
6. Performance overlay  7. Ely.by / authlib-injector  8. Hindi/Hinglish strings  9. Auto-updater
