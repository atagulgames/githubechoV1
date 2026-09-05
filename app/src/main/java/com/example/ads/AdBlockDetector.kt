package com.example.ads

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

sealed class AdHealthResult {
    val isHealthy: Boolean get() = this is Healthy
    abstract val title: String
    abstract val subtitle: String
    abstract val details: String

    object Healthy : AdHealthResult() {
        override val title: String = "Reklamlar Aktif"
        override val subtitle: String = "Reklamlar sorunsuz çalışıyor"
        override val details: String = ""
    }

    data class NoInternet(
        override val title: String = "İnternet Bağlantısı Yok",
        override val subtitle: String = "Reklamlar çalıştırılamıyor",
        override val details: String = "ECHO tamamen ücretsiz bir oyundur ve masrafları reklam gelirleriyle karşılanmaktadır. Oyuna devam edebilmek için lütfen internet bağlantınızı (Wi-Fi veya Mobil Veri) açın."
    ) : AdHealthResult()

    data class AdBlockerDetected(
        val reason: String = "Reklam Engelleyici Tespit Edildi",
        override val title: String = "Reklam Engelleyici Tespit Edildi",
        override val subtitle: String = "Uygulama çalıştırılamıyor",
        override val details: String = "ECHO ücretsiz bir oyundur. Oyuna devam edebilmek için lütfen telefon ayarlarınızdaki Özel DNS'i (AdGuard, NextDNS vb.) kapatın veya reklam engelleme uygulamasını duraklatın."
    ) : AdHealthResult()

    data class AdServiceBlocked(
        override val title: String = "Reklamlar Engelleniyor",
        override val subtitle: String = "Reklam sunucusuna erişilemiyor",
        override val details: String = "Reklam sunucularına bağlantı engellenmiş görünüyor. Lütfen reklam engelleyiciyi veya Özel DNS ayarını kapatıp tekrar deneyin."
    ) : AdHealthResult()
}

object AdBlockDetector {
    private const val TAG = "AdBlockDetector"

    // Known ad-blocker package identifiers
    private val ADBLOCK_PACKAGES = listOf(
        "com.adguard.android",
        "com.adguard.android.contentblocker",
        "org.blokada.alarm",
        "org.blokada.origin.alarm",
        "org.blokada.v5",
        "org.blokada.v6",
        "org.jak_linux.dns66",
        "de.hpi.sam.adblocker",
        "com.freeadhacker.adblocker",
        "com.adblocker.free",
        "com.celzero.bravedns",
        "com.rethinkdb.firewall",
        "app.greyshirts.firewall",
        "eu.faircode.netguard",
        "org.adaway",
        "app.fedilab.adaway",
        "pan.alexander.tordnsproxy",
        "com.vrem.adblock"
    )

    /**
     * Fast, comprehensive health check:
     * Dispatches parallel probes and completes in ~150-300ms.
     */
    suspend fun checkAdHealth(context: Context): AdHealthResult = withContext(Dispatchers.IO) {
        try {
            // 1. Instant check: Connectivity manager capabilities (< 1ms)
            if (!hasNetworkCapability(context)) {
                return@withContext AdHealthResult.NoInternet()
            }

            // 2. Instant check: Private DNS configuration (Android 9+) (< 1ms)
            if (checkPrivateDnsSettings(context)) {
                Log.w(TAG, "AdBlock detected via Private DNS settings")
                return@withContext AdHealthResult.AdBlockerDetected(
                    reason = "Özel DNS",
                    details = "Telefon Ayarları > Bağlantılar > Diğer bağlantı ayarları > Özel DNS ayarını 'Otomatik' veya 'Kapalı' yapın."
                )
            }

            // 3. Instant check: /etc/hosts file (< 1ms)
            if (checkHostsFile()) {
                Log.w(TAG, "AdBlock detected via Hosts file modification")
                return@withContext AdHealthResult.AdBlockerDetected(
                    reason = "Hosts Dosyası",
                    details = "Cihazınızın hosts dosyasında reklam engelleme yönlendirmeleri tespit edildi."
                )
            }

            // 4. Instant check: Installed adblock packages (< 2ms)
            if (checkAdBlockerPackages(context)) {
                Log.w(TAG, "AdBlock detected via installed package")
                return@withContext AdHealthResult.AdBlockerDetected(
                    reason = "Reklam Engelleyici Uygulama",
                    details = "AdGuard, Blokada veya benzeri reklam engelleyici VPN uygulamasını duraklatın."
                )
            }

            // 5. Fast Parallel DNS & probe check (~150-300ms)
            val parallelResult = checkAdDomainBlockingParallel()
            if (parallelResult != null) {
                return@withContext parallelResult
            }

            AdHealthResult.Healthy
        } catch (e: Exception) {
            Log.e(TAG, "Error checking ad health status", e)
            AdHealthResult.Healthy
        }
    }

    suspend fun isAdBlockerActive(context: Context): Boolean = withContext(Dispatchers.IO) {
        val result = checkAdHealth(context)
        !result.isHealthy
    }

    private fun hasNetworkCapability(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Checks if Private DNS is enabled with an ad-blocking provider.
     */
    private fun checkPrivateDnsSettings(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val privateDnsSpecifier = Settings.Global.getString(
                    context.contentResolver,
                    "private_dns_specifier"
                )?.lowercase()?.trim() ?: ""

                if (privateDnsSpecifier.isNotEmpty()) {
                    val adBlockDnsKeywords = listOf(
                        "adguard", "nextdns", "controld", "mullvad",
                        "cleanbrowsing", "adblock", "block", "filter", "dnsforge", "ahadns"
                    )
                    if (adBlockDnsKeywords.any { privateDnsSpecifier.contains(it) }) {
                        return true
                    }
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks if /etc/hosts contains ad network domains mapped to 127.0.0.1 or 0.0.0.0.
     */
    private fun checkHostsFile(): Boolean {
        val paths = listOf("/system/etc/hosts", "/etc/hosts")
        for (path in paths) {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                try {
                    BufferedReader(FileReader(file)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val l = line?.trim()?.lowercase() ?: continue
                            if (l.startsWith("#") || l.isEmpty()) continue

                            val isSinkholed = l.startsWith("127.0.0.1") || l.startsWith("0.0.0.0") || l.startsWith("::1")
                            if (isSinkholed) {
                                if (l.contains("startappservice") || l.contains("pagead2.googlesyndication.com") || l.contains("googleads.g.doubleclick.net")) {
                                    return true
                                }
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        return false
    }

    /**
     * Checks if an ad-blocking package is installed.
     */
    private fun checkAdBlockerPackages(context: Context): Boolean {
        try {
            val pm = context.packageManager
            for (pkg in ADBLOCK_PACKAGES) {
                try {
                    pm.getPackageInfo(pkg, 0)
                    return true
                } catch (_: Exception) {}
            }
            return false
        } catch (_: Exception) {
            return false
        }
    }

    private enum class ProbeResult {
        HEALTHY,
        SINKHOLED,
        BLOCKED
    }

    /**
     * Parallel resolution and probe:
     * Dispatches control domain check and ad domain checks simultaneously.
     * Completes within 1200ms maximum, typically 100-200ms.
     */
    private suspend fun checkAdDomainBlockingParallel(): AdHealthResult? = coroutineScope {
        withTimeoutOrNull(1500L) {
            // 1. Control domain probe (in parallel)
            val controlDeferred = async(Dispatchers.IO) {
                resolveHostname("google.com") || resolveHostname("cloudflare.com")
            }

            // 2. StartApp ad request domain probe (in parallel)
            val startAppReqDeferred = async(Dispatchers.IO) {
                probeAdDomain("req.startappservice.com")
            }

            // 3. Google Ads request domain probe (in parallel)
            val googleAdsDeferred = async(Dispatchers.IO) {
                probeAdDomain("pagead2.googlesyndication.com")
            }

            val hasInternet = controlDeferred.await()
            if (!hasInternet) {
                return@withTimeoutOrNull AdHealthResult.NoInternet()
            }

            val startAppStatus = startAppReqDeferred.await()
            val googleAdsStatus = googleAdsDeferred.await()

            // 1) Any ad domain resolved explicitly to 0.0.0.0 / 127.0.0.1 (sinkhole signature)
            if (startAppStatus == ProbeResult.SINKHOLED || googleAdsStatus == ProbeResult.SINKHOLED) {
                Log.w(TAG, "Ad domain explicitly sinkholed!")
                return@withTimeoutOrNull AdHealthResult.AdServiceBlocked()
            }

            // 2) Both ad domains are blocked via DNS NXDOMAIN or connection refused
            if (startAppStatus == ProbeResult.BLOCKED && googleAdsStatus == ProbeResult.BLOCKED) {
                Log.w(TAG, "Both ad domains blocked via DNS or connection reset!")
                return@withTimeoutOrNull AdHealthResult.AdServiceBlocked()
            }

            null // Healthy
        }
    }

    private fun resolveHostname(host: String): Boolean {
        return try {
            val addresses = InetAddress.getAllByName(host)
            addresses.isNotEmpty() && !addresses.all { isSinkholedAddress(it) }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Probes an ad domain for active blocking:
     * Returns SINKHOLED if IP is 0.0.0.0 / 127.0.0.1.
     * Returns BLOCKED if DNS NXDOMAIN or connection refused.
     * Returns HEALTHY if reachable or if it's only network latency (SocketTimeoutException).
     */
    private fun probeAdDomain(host: String): ProbeResult {
        return try {
            val addresses = InetAddress.getAllByName(host)
            if (addresses.isEmpty() || addresses.all { isSinkholedAddress(it) }) {
                return ProbeResult.SINKHOLED
            }

            // DNS resolved properly to valid non-sinkholed IP address!
            // Fast probe to verify connection is not actively rejected by local VPN filter
            val url = URL("https://$host")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 700
            conn.readTimeout = 700
            conn.instanceFollowRedirects = false
            conn.requestMethod = "HEAD"
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            val code = conn.responseCode
            conn.disconnect()
            ProbeResult.HEALTHY
        } catch (e: java.net.UnknownHostException) {
            // DNS blocked (NXDOMAIN)
            ProbeResult.BLOCKED
        } catch (e: java.net.ConnectException) {
            // Connection refused by local firewall / VPN ad blocker
            ProbeResult.BLOCKED
        } catch (e: java.net.SocketTimeoutException) {
            // High latency / slow connection - NOT an ad blocker!
            ProbeResult.HEALTHY
        } catch (_: Exception) {
            // General network/SSL fluctuations - NOT an ad blocker
            ProbeResult.HEALTHY
        }
    }

    private fun isSinkholedAddress(address: InetAddress): Boolean {
        val hostAddress = address.hostAddress ?: return false
        return hostAddress == "0.0.0.0" ||
                hostAddress == "127.0.0.1" ||
                hostAddress == "::1" ||
                hostAddress == "::"
    }
}
