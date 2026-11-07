package com.example.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

data class CachedNativeAd(
    val ad: NativeAd,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * NativeAdManager manages an in-memory cache and dispatcher for AdMob Native Ads.
 * Key capabilities:
 * 1. Persistent caching across screen navigations (Home <-> JobDetail <-> Companies, etc.)
 *    so ads don't re-request, flash, or disappear when users move between pages.
 * 2. Multi-listener fanout: multiple UI components requesting the same slot await the single
 *    in-flight request without dropping callbacks.
 * 3. Smart fallback: if a unit ID returns NO_FILL or error, it attempts fallback to an
 *    alternate production unit so the placement is reliably filled.
 * 4. Prewarming: core placements (JobDetail 0-4, Home 0-4, CareerRedirect) are prewarmed on launch.
 */
object NativeAdManager {
    private const val TAG = "NativeAdManager"

    private val adCache = ConcurrentHashMap<String, CachedNativeAd>()
    private val inFlightWaiters = ConcurrentHashMap<String, CopyOnWriteArrayList<Pair<(NativeAd) -> Unit, () -> Unit>>>()
    private val failureCooldownMap = ConcurrentHashMap<String, Long>()
    private val mainHandler = Handler(Looper.getMainLooper())

    // Maximum cache age: 50 minutes (AdMob ads remain valid for up to 60 minutes)
    private const val MAX_CACHE_AGE_MS = 50 * 60 * 1000L

    // Cooldown duration between retry attempts on failed loads (10 seconds)
    private const val RETRY_COOLDOWN_MS = 10 * 1000L

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
     * Loads a native ad for the given slotKey if not already cached.
     * Reuses cached ads across screen navigation and avoids duplicate network requests.
     */
    fun loadAdForSlot(
        context: Context,
        slotKey: String,
        adUnitId: String,
        onAdReady: (NativeAd) -> Unit,
        onAdFailed: () -> Unit = {}
    ) {
        // 1. Check if ad is already cached and valid
        val existing = getCachedAd(slotKey)
        if (existing != null) {
            onAdReady(existing)
            return
        }

        // 2. Check if a request for this slot is already in flight
        val existingWaiters = inFlightWaiters[slotKey]
        if (existingWaiters != null) {
            existingWaiters.add(Pair(onAdReady, onAdFailed))
            return
        }

        // 3. Check failure cooldown (to avoid rapid error spamming)
        val lastFailure = failureCooldownMap[slotKey] ?: 0L
        if (System.currentTimeMillis() - lastFailure < RETRY_COOLDOWN_MS) {
            onAdFailed()
            return
        }

        // 4. Register new in-flight waiter list
        val waiters = CopyOnWriteArrayList<Pair<(NativeAd) -> Unit, () -> Unit>>()
        waiters.add(Pair(onAdReady, onAdFailed))
        inFlightWaiters[slotKey] = waiters

        // 5. Build and launch AdLoader
        requestAd(context.applicationContext, slotKey, adUnitId, attemptFallback = true)
    }

    private fun requestAd(
        appContext: Context,
        slotKey: String,
        adUnitId: String,
        attemptFallback: Boolean
    ) {
        val loader = AdLoader.Builder(appContext, adUnitId)
            .forNativeAd { ad ->
                val old = adCache.put(slotKey, CachedNativeAd(ad))
                if (old != null && old.ad !== ad) {
                    old.ad.destroy()
                }

                val registered = inFlightWaiters.remove(slotKey)
                mainHandler.post {
                    registered?.forEach { (onSuccess, _) ->
                        try {
                            onSuccess(ad)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error invoking onAdReady for $slotKey: ${e.message}")
                        }
                    }
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Ad failed to load for slot $slotKey (unit $adUnitId): ${error.message} (code ${error.code})")

                    if (attemptFallback && !AdConfig.isTestMode) {
                        // Attempt fallback to Unit 1 or Unit 2 to ensure slot gets filled
                        val fallbackUnit = if (adUnitId != AdConfig.PROD_NATIVE_UNIT_1) {
                            AdConfig.PROD_NATIVE_UNIT_1
                        } else {
                            AdConfig.PROD_NATIVE_UNIT_2
                        }
                        Log.d(TAG, "Attempting fallback for slot $slotKey with unit $fallbackUnit")
                        requestAd(appContext, slotKey, fallbackUnit, attemptFallback = false)
                        return
                    }

                    failureCooldownMap[slotKey] = System.currentTimeMillis()
                    val registered = inFlightWaiters.remove(slotKey)
                    mainHandler.post {
                        registered?.forEach { (_, onFail) ->
                            try {
                                onFail()
                            } catch (e: Exception) {
                                Log.w(TAG, "Error invoking onAdFailed for $slotKey: ${e.message}")
                            }
                        }
                    }
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

    /**
     * Prewarms key ad placements in the background on app start so ads are ready
     * immediately when navigating to JobDetailScreen, Home feed, or CareerRedirect.
     */
    fun prewarmCoreSlots(context: Context) {
        val coreSlots = listOf(
            // Home Feed
            Pair("home_feed_ad_0", AdConfig.getNativeAdUnitId(0)),
            Pair("home_feed_ad_1", AdConfig.getNativeAdUnitId(1)),
            Pair("home_feed_ad_2", AdConfig.getNativeAdUnitId(2)),
            Pair("home_feed_ad_3", AdConfig.getNativeAdUnitId(3)),
            Pair("home_feed_ad_4", AdConfig.getNativeAdUnitId(4)),
            // Job Details
            Pair("job_detail_ad_0", AdConfig.getNativeAdUnitId(0)),
            Pair("job_detail_ad_1", AdConfig.getNativeAdUnitId(1)),
            Pair("job_detail_ad_2", AdConfig.getNativeAdUnitId(2)),
            Pair("job_detail_ad_3", AdConfig.getNativeAdUnitId(3)),
            Pair("job_detail_ad_4", AdConfig.getNativeAdUnitId(4)),
            // Career Redirect
            Pair("career_redirect_native_ad", AdConfig.PROD_NATIVE_UNIT_1),
            // All Stories Page & Story Reel
            Pair("all_stories_grid_0", AdConfig.getNativeAdUnitId(0)),
            Pair("all_stories_grid_1", AdConfig.getNativeAdUnitId(1)),
            Pair("home_story_reel_pos3", AdConfig.PROD_NATIVE_UNIT_1),
            // Companies Page (Directory & Profile/Jobs)
            Pair("company_dir_ad_2", AdConfig.getNativeAdUnitId(1)),
            Pair("company_dir_ad_5", AdConfig.getNativeAdUnitId(2)),
            Pair("company_detail_top_ad", AdConfig.getNativeAdUnitId(0)),
            Pair("company_detail_bottom_ad", AdConfig.getNativeAdUnitId(1)),
            Pair("company_job_ad_2", AdConfig.getNativeAdUnitId(2)),
            // Jobs List Screen
            Pair("jobs_list_ad_1", AdConfig.getNativeAdUnitId(1)),
            Pair("jobs_list_ad_3", AdConfig.getNativeAdUnitId(2)),
            Pair("jobs_list_ad_5", AdConfig.getNativeAdUnitId(3))
        )

        coreSlots.forEachIndexed { idx, (slot, unit) ->
            // Stagger prewarm requests slightly (120ms) to prevent burst throttling on cold start
            mainHandler.postDelayed({
                loadAdForSlot(
                    context = context,
                    slotKey = slot,
                    adUnitId = unit,
                    onAdReady = { Log.d(TAG, "Prewarmed ad ready for slot: $slot") },
                    onAdFailed = { Log.d(TAG, "Prewarm failed for slot: $slot") }
                )
            }, (idx * 120L))
        }
    }
}
