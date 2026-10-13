package com.rcempire.empirelauncher.crash

data class CrashHint(val title: String, val fix: String)

object CrashAnalyzer {
    private val rules: List<Pair<Regex, CrashHint>> = listOf(
        Regex("OutOfMemoryError|GC overhead") to CrashHint("Game ran out of RAM", "Increase RAM in instance settings (keep below ~45% of device RAM) or lower render distance."),
        Regex("UnsupportedClassVersionError") to CrashHint("Wrong Java version", "Switch Java: MC 1.20.5+ needs Java 21, 1.17-1.20.4 needs 17, older needs 8."),
        Regex("GLFW error|OpenGL|EGL|GLX") to CrashHint("Renderer problem", "Change the renderer (try GL4ES, MobileGlues or Zink) in instance settings."),
        Regex("Mixin apply failed|InvalidInjectionException") to CrashHint("Mod conflict", "A mod doesn't match your MC version or another mod. Disable recently added mods."),
        Regex("Missing or unsupported mandatory dependencies") to CrashHint("Missing mod dependency", "Install the required dependency (e.g. Fabric API) for the right version."),
        Regex("Could not reserve enough space|Invalid maximum heap size") to CrashHint("RAM value too high", "Lower RAM allocation for this instance."),
        Regex("Exit code: 137|SIGKILL") to CrashHint("Android killed the game", "Device ran out of memory. Reduce RAM / render distance and close background apps.")
    )
    fun analyze(log: String): List<CrashHint> =
        rules.filter { it.first.containsMatchIn(log) }.map { it.second }
            .ifEmpty { listOf(CrashHint("Unknown crash", "Export the log and share it with the RC Empire community for help.")) }
}
