package com.example.ads

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import java.util.concurrent.ConcurrentHashMap

data class CachedNativeAd(
    val ad: NativeAd,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * NativeAdManager manages an in-memory cache of loaded AdMob Native Ads.
 * This prevents ads from being repeatedly re-requested on every screen visit or recomposition,
 * eliminates the "flash and disappear" issue caused by duplicate request throttling,
 * and ensures ads stay visible and immediately available when navigating back to previous pages.
 */
object NativeAdManager {
    private val adCache = ConcurrentHashMap<String, CachedNativeAd>()
    private val inFlightKeys = ConcurrentHashMap<String, Boolean>()
    private val cooldownMap = ConcurrentHashMap<String, Long>()

    // Maximum cache age: 50 minutes (AdMob ads remain valid for up to 60 minutes)
    private const val MAX_CACHE_AGE_MS = 50 * 60 * 1000L

    // Cooldown duration between retry attempts on failed loads (20 seconds)
    private const val RETRY_COOLDOWN_MS = 20 * 1000L

    /**
     * Retrieves an existing valid cached NativeAd for the given slot.
     */
    fun getCachedAd(slotKey: String): NativeAd? {
        val entry = adCache[slotKey] ?: return null
        if (System.currentTimeMillis() - entry.timestamp > MAX_CACHE_AGE_MS) {
            adCache.remove(slotKey)
            entry.ad.destroy()
            return null
        }
        return entry.ad
    }

    /**
     * Loads a native ad into the cache for the given slotKey if not already cached.
     * Reuses cached ads across screen navigation and avoids duplicate network requests.
     */
    fun loadAdForSlot(
        context: Context,
        slotKey: String,
        adUnitId: String,
        onAdReady: (NativeAd) -> Unit,
        onAdFailed: () -> Unit = {}
    ) {
        val existing = getCachedAd(slotKey)
        if (existing != null) {
            onAdReady(existing)
            return
        }

        val lastFailure = cooldownMap[slotKey] ?: 0L
        if (System.currentTimeMillis() - lastFailure < RETRY_COOLDOWN_MS) {
            onAdFailed()
            return
        }

        if (inFlightKeys.putIfAbsent(slotKey, true) == true) {
            return
        }

        val loader = AdLoader.Builder(context.applicationContext, adUnitId)
            .forNativeAd { ad ->
                inFlightKeys.remove(slotKey)
                val old = adCache.put(slotKey, CachedNativeAd(ad))
                if (old != null && old.ad !== ad) {
                    old.ad.destroy()
                }
                onAdReady(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    inFlightKeys.remove(slotKey)
                    cooldownMap[slotKey] = System.currentTimeMillis()
                    onAdFailed()
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()

        loader.loadAd(AdRequest.Builder().build())
    }
}
