# runtime/ (native game layer) - NOT implemented yet

Running Minecraft Java on Android needs: a bundled JRE (8/17/21 for arm64), a JNI launcher that
starts the JVM, and renderer libraries (GL4ES, MobileGlues, Zink, ...). Writing this from scratch is
a multi-month job, so the plan is to reuse the GPL-3.0 code of ZalithLauncher / PojavLauncher:

1. Add ZalithLauncher (https://github.com/ZalithLauncher/ZalithLauncher2) as a git submodule or copy
   its `jre_lwjgl3glfw`, `app_pojavlauncher/src/main/jni` and renderer modules.
2. Keep the GPL-3.0 license and credit them (already in About screen + CREDITS.md).
3. Replace `LaunchCommandBuilder` output with a call into their JNI `JLaunchInfo` / `JREUtils.launchJavaVM`.
4. Bundle JRE tarballs as assets (download at first run to keep APK small).
