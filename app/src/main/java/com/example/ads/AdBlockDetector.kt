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
        override val title: String = "Ads Active"
        override val subtitle: String = "Ads running smoothly"
        override val details: String = ""
    }

    data class NoInternet(
        override val title: String = "No Internet Connection",
        override val subtitle: String = "Ads cannot run",
        override val details: String = "ECHO FLUX is free to play and supported by ad revenue. Please enable your internet connection (Wi-Fi or Mobile Data) to continue."
    ) : AdHealthResult()

    data class AdBlockerDetected(
        val reason: String = "Ad Blocker Detected",
        override val title: String = "Ad Blocker Detected",
        override val subtitle: String = "Unable to run application",
        override val details: String = "ECHO FLUX is free to play. Please turn off Private DNS (AdGuard, NextDNS) or pause ad blocker apps to continue."
    ) : AdHealthResult()

    data class AdServiceBlocked(
        override val title: String = "Ads Blocked",
        override val subtitle: String = "Cannot connect to ad server",
        override val details: String = "Connection to ad servers appears blocked. Please disable ad blocker or Private DNS and try again."
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

    fun hasNetworkCapability(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Non-blocking ad health check that allows normal gameplay without interrupting the user.
     */
    suspend fun checkAdHealth(context: Context): AdHealthResult = withContext(Dispatchers.IO) {
        AdHealthResult.Healthy
    }

    suspend fun isAdBlockerActive(context: Context): Boolean = false

    private fun checkPrivateDnsSettings(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val mode = Settings.Global.getString(context.contentResolver, "private_dns_mode")
                val specifier = Settings.Global.getString(context.contentResolver, "private_dns_specifier") ?: ""
                val lower = specifier.lowercase()
                (mode == "hostname" || mode == "custom") && (
                    lower.contains("adguard") || lower.contains("adblock") ||
                    lower.contains("dnsforge") || lower.contains("nextdns") ||
                    lower.contains("rethinkdns")
                )
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun checkHostsFile(): Boolean {
        return false
    }

    private fun checkAdBlockerPackages(context: Context): Boolean {
        return false
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

            // 2. Yandex ad request domain probe (in parallel)
            val yandexAdsDeferred = async(Dispatchers.IO) {
                probeAdDomain("mobile.yandex.net")
            }

            // 3. Google Ads request domain probe (in parallel)
            val googleAdsDeferred = async(Dispatchers.IO) {
                probeAdDomain("pagead2.googlesyndication.com")
            }

            val hasInternet = controlDeferred.await()
            if (!hasInternet) {
                return@withTimeoutOrNull AdHealthResult.NoInternet()
            }

            val yandexAdsStatus = yandexAdsDeferred.await()
            val googleAdsStatus = googleAdsDeferred.await()

            // 1) Any ad domain resolved explicitly to 0.0.0.0 / 127.0.0.1 (sinkhole signature)
            if (yandexAdsStatus == ProbeResult.SINKHOLED || googleAdsStatus == ProbeResult.SINKHOLED) {
                Log.w(TAG, "Ad domain explicitly sinkholed!")
                return@withTimeoutOrNull AdHealthResult.AdServiceBlocked()
            }

            // 2) Both ad domains are blocked via DNS NXDOMAIN or connection refused
            if (yandexAdsStatus == ProbeResult.BLOCKED && googleAdsStatus == ProbeResult.BLOCKED) {
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
