package com.example

import android.app.Application
import android.util.Log
import com.example.ads.AppOpenAdManager
import com.example.ads.InterstitialAdManager
import com.google.android.gms.ads.MobileAds

class JobsReportApp : Application() {

    lateinit var appOpenAdManager: AppOpenAdManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this) { initializationStatus ->
            Log.d(TAG, "AdMob SDK Initialized: $initializationStatus")
        }

        // Initialize App Open Ad Manager & Interstitial Ad Manager
        appOpenAdManager = AppOpenAdManager(this)
        appOpenAdManager.loadAd(this)
        InterstitialAdManager.loadAd(this)
    }

    companion object {
        private const val TAG = "JobsReportApp"
        lateinit var instance: JobsReportApp
            private set
    }
}
