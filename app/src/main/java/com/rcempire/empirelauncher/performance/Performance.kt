package com.rcempire.empirelauncher.performance

data class OptimizeResult(val ramMb: Int, val jvmArgs: String, val renderDistance: Int, val renderer: String)

object SmartOptimizer {
    /** Safe RAM: at most ~45% of device RAM (Android needs the rest), clamped to 1-4 GB. */
    fun optimize(totalRamMb: Int, gpu: String, mcVersion: String): OptimizeResult {
        val ram = (totalRamMb * 45 / 100).coerceIn(1024, 4096) / 256 * 256
        val rd = when { ram >= 3584 -> 12; ram >= 2560 -> 8; else -> 6 }
        return OptimizeResult(ram, jvmFlags(ram), rd, RendererAdvisor.recommend(gpu, mcVersion))
    }
    fun jvmFlags(ramMb: Int) = buildString {
        append("-XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:+UnlockExperimentalVMOptions ")
        append("-XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:+DisableExplicitGC -XX:+ParallelRefProcEnabled")
        if (ramMb < 2048) append(" -XX:+UseStringDeduplication")
    }
}

object RendererAdvisor {
    val all = listOf("GL4ES", "Holy GL4ES", "ANGLE", "Zink (Vulkan)", "VirGL", "MobileGlues", "Krypton Wrapper")
    fun recommend(gpu: String, mcVersion: String): String {
        val g = gpu.lowercase()
        val modern = (mcVersion.split('.').getOrNull(1)?.toIntOrNull() ?: 21) >= 17
        return when {
            "adreno" in g -> if (modern) "Zink (Vulkan)" else "GL4ES"
            "mali" in g -> if (modern) "MobileGlues" else "Holy GL4ES"
            else -> "GL4ES"
        }
    }
    fun shaderWarning(renderer: String) =
        if (renderer.startsWith("GL4ES") || renderer == "VirGL")
            "Shaders (Iris/OptiFine) may not work with $renderer. Try Zink or MobileGlues." else null
}
