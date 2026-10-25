package com.example.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

class AppOpenAdManager(private val application: Application) :
    Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var loadTime: Long = 0L
    private var currentActivity: Activity? = null

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private fun isAdAvailable(): Boolean {
        val wasLoadedRecently = (Date().time - loadTime) < (4 * 3600 * 1000)
        return appOpenAd != null && wasLoadedRecently
    }

    fun loadAd(context: Context) {
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()
        val adUnitId = AdConfig.appOpenAdUnitId

        AppOpenAd.load(
            context.applicationContext,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App Open Ad loaded successfully")
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "App Open Ad failed to load: ${loadAdError.message}")
                    isLoadingAd = false
                }
            }
        )
    }

    fun showAdIfAvailable(activity: Activity, onShowComplete: () -> Unit = {}) {
        if (!AdStateManager.canShowAppOpen()) {
            Log.d(TAG, "Cannot show App Open ad: another ad is currently showing")
            onShowComplete()
            return
        }

        if (!isAdAvailable()) {
            Log.d(TAG, "App Open Ad is not ready yet")
            onShowComplete()
            loadAd(activity)
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                AdStateManager.setAppOpenShowing(true)
            }

            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                AdStateManager.setAppOpenShowing(false)
                loadAd(activity)
                onShowComplete()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "App Open Ad failed to show: ${adError.message}")
                appOpenAd = null
                AdStateManager.setAppOpenShowing(false)
                loadAd(activity)
                onShowComplete()
            }
        }

        appOpenAd?.show(activity)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        currentActivity?.let { activity ->
            showAdIfAvailable(activity)
        }
    }

    // Activity Lifecycle Callbacks
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }

    companion object {
        private const val TAG = "AppOpenAdManager"
    }
}
