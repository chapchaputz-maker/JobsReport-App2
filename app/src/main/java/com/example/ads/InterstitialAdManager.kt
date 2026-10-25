package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdManager {
    private const val TAG = "InterstitialAdManager"
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var lastAdShownTime: Long = 0L

    fun loadAd(context: Context) {
        if (interstitialAd != null || isLoading) {
            return
        }

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        val adUnitId = AdConfig.interstitialAdUnitId

        InterstitialAd.load(
            context.applicationContext,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    private var applyActionCount = 0

    fun handleApplyAction(
        activity: Activity,
        onProceedToApplication: () -> Unit
    ) {
        applyActionCount++
        val shouldShowAd = applyActionCount >= AdConfig.APPLY_ACTIONS_BEFORE_INTERSTITIAL

        val currentTime = SystemClock.elapsedRealtime()
        val isCooldownActive = (currentTime - lastAdShownTime) < AdConfig.INTERSTITIAL_COOLDOWN_MS
        val currentAd = interstitialAd

        if (!shouldShowAd || currentAd == null || !AdStateManager.canShowInterstitial() || isCooldownActive) {
            // If ad was not loaded, kick off preload for future actions
            if (currentAd == null) {
                loadAd(activity)
            }
            onProceedToApplication()
            return
        }

        // Reset counter when showing interstitial
        applyActionCount = 0

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                AdStateManager.setInterstitialShowing(true)
                lastAdShownTime = SystemClock.elapsedRealtime()
            }

            override fun onAdDismissedFullScreenContent() {
                AdStateManager.setInterstitialShowing(false)
                interstitialAd = null
                loadAd(activity)
                onProceedToApplication()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Interstitial ad failed to show: ${adError.message}")
                AdStateManager.setInterstitialShowing(false)
                interstitialAd = null
                loadAd(activity)
                onProceedToApplication()
            }
        }

        currentAd.show(activity)
    }

    fun showAdIfReady(
        activity: Activity,
        onAdDismissedOrSkipped: () -> Unit
    ) {
        val currentTime = SystemClock.elapsedRealtime()
        val isCooldownActive = (currentTime - lastAdShownTime) < AdConfig.INTERSTITIAL_COOLDOWN_MS
        val currentAd = interstitialAd

        if (currentAd == null || !AdStateManager.canShowInterstitial() || isCooldownActive) {
            // Never block the user action if ad is not ready or cooldown is active
            if (currentAd == null) {
                loadAd(activity)
            }
            onAdDismissedOrSkipped()
            return
        }

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                AdStateManager.setInterstitialShowing(true)
                lastAdShownTime = SystemClock.elapsedRealtime()
            }

            override fun onAdDismissedFullScreenContent() {
                AdStateManager.setInterstitialShowing(false)
                interstitialAd = null
                loadAd(activity)
                onAdDismissedOrSkipped()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Interstitial ad failed to show: ${adError.message}")
                AdStateManager.setInterstitialShowing(false)
                interstitialAd = null
                loadAd(activity)
                onAdDismissedOrSkipped()
            }
        }

        currentAd.show(activity)
    }
}
