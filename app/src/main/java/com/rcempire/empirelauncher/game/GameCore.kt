package com.rcempire.empirelauncher.game

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest

/** Download with resume (HTTP Range) + SHA-1 verification. Use official Mojang URLs only. */
class Downloader(private val http: OkHttpClient) {
    suspend fun download(url: String, dest: File, sha1: String? = null, onProgress: (Long, Long) -> Unit = { _, _ -> }) =
        withContext(Dispatchers.IO) {
            require(url.startsWith("https://")) { "Insecure URL refused" }
            dest.parentFile?.mkdirs()
            val part = File(dest.path + ".part")
            val have = if (part.exists()) part.length() else 0L
            val req = Request.Builder().url(url).apply { if (have > 0) header("Range", "bytes=$have-") }.build()
            http.newCall(req).execute().use { resp ->
                check(resp.isSuccessful) { "Download failed: HTTP ${resp.code}" }
                val resumed = resp.code == 206
                val total = (resp.body!!.contentLength().takeIf { it > 0 } ?: 0L) + if (resumed) have else 0L
                RandomAccessFile(part, "rw").use { raf ->
                    if (resumed) raf.seek(have) else raf.setLength(0)
                    var done = if (resumed) have else 0L
                    resp.body!!.byteStream().use { input ->
                        val buf = ByteArray(64 * 1024)
                        while (true) {
                            val n = input.read(buf); if (n < 0) break
                            raf.write(buf, 0, n); done += n; onProgress(done, total)
                        }
                    }
                }
            }
            if (sha1 != null && !sha1Of(part).equals(sha1, true)) {
                part.delete(); error("SHA-1 mismatch for ${dest.name}")
            }
            part.renameTo(dest)
        }

    private fun sha1Of(f: File): String {
        val md = MessageDigest.getInstance("SHA-1")
        f.inputStream().use { s -> val b = ByteArray(64 * 1024); while (true) { val n = s.read(b); if (n < 0) break; md.update(b, 0, n) } }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}

object JavaSelector {
    /** MC <=1.16 -> Java 8, 1.17-1.20.4 -> 17, 1.20.5+ -> 21 */
    fun forMinecraft(id: String): Int {
        val p = id.split('.', '-').mapNotNull { it.toIntOrNull() }
        val minor = p.getOrNull(1) ?: return 21 // snapshots like "24w14a": assume newest
        val patch = p.getOrNull(2) ?: 0
        return when {
            minor <= 16 -> 8
            minor < 20 || (minor == 20 && patch <= 4) -> 17
            else -> 21
        }
    }
}

data class Instance(
    val id: String, val name: String, val mcVersion: String, val loader: String = "vanilla",
    val ramMb: Int = 2048, val renderer: String = "GL4ES",
    val javaVersion: Int = JavaSelector.forMinecraft(mcVersion), val jvmArgs: String = ""
)

/**
 * Builds the Java command line. Actually starting the JVM on Android needs the native runtime
 * (JRE + JNI launcher + renderer libs) from the ZalithLauncher/PojavLauncher code base.
 * See runtime/README.md.
 */
object LaunchCommandBuilder {
    fun build(i: Instance, javaBin: String, gameDir: String, classpath: String, mainClass: String,
              username: String, uuid: String, accessToken: String, assetsDir: String, assetIndex: String): List<String> =
        listOf(javaBin, "-Xmx${i.ramMb}M", "-Xms${i.ramMb / 2}M") +
            i.jvmArgs.split(' ').filter { it.isNotBlank() } +
            listOf("-cp", classpath, mainClass,
                "--username", username, "--uuid", uuid, "--accessToken", accessToken,
                "--version", i.mcVersion, "--gameDir", gameDir, "--assetsDir", assetsDir,
                "--assetIndex", assetIndex, "--userType", "msa")
}
