package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

/**
 * RewardedAdManager coordinates loading and displaying AdMob Rewarded Video Ads.
 * Used for story viewer gating (unlocking more stories after viewing 3 stories).
 */
object RewardedAdManager {
    private const val TAG = "RewardedAdManager"
    private var rewardedAd: RewardedAd? = null
    private val isLoading = AtomicBoolean(false)

    fun isAdLoaded(): Boolean = rewardedAd != null

    /**
     * Preloads a Rewarded Ad into memory.
     */
    fun loadAd(context: Context, onLoaded: (() -> Unit)? = null) {
        if (rewardedAd != null) {
            onLoaded?.invoke()
            return
        }

        if (!isLoading.compareAndSet(false, true)) {
            // Load is already in flight
            return
        }

        val adUnitId = AdConfig.rewardedAdUnitId
        val request = AdRequest.Builder().build()

        RewardedAd.load(
            context.applicationContext,
            adUnitId,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully for unit: $adUnitId")
                    rewardedAd = ad
                    isLoading.set(false)
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Rewarded ad failed to load: ${loadAdError.message} (code: ${loadAdError.code})")
                    rewardedAd = null
                    isLoading.set(false)
                }
            }
        )
    }

    /**
     * Displays the rewarded ad to the user.
     * If an ad is not ready, it triggers loading and gracefully awards the unlock
     * so user flow is never blocked by network latency or no-fill errors.
     */
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        val currentAd = rewardedAd

        if (currentAd == null) {
            Log.w(TAG, "Rewarded ad not cached when requested; preloading and granting pass")
            loadAd(activity)
            onUserEarnedReward()
            onAdDismissed()
            return
        }

        var rewardAwarded = false

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showed fullscreen")
                AdStateManager.setInterstitialShowing(true)
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed by user")
                AdStateManager.setInterstitialShowing(false)
                rewardedAd = null
                // Immediately preload the next rewarded ad for future unlocks
                loadAd(activity)
                if (rewardAwarded) {
                    onUserEarnedReward()
                }
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Rewarded ad failed to show: ${adError.message}")
                AdStateManager.setInterstitialShowing(false)
                rewardedAd = null
                loadAd(activity)
                // Award pass on display failure so user is never permanently stuck
                onUserEarnedReward()
                onAdDismissed()
            }
        }

        currentAd.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            rewardAwarded = true
        }
    }
}
