package com.example.ads

object AdConfig {
    // Production AdMob IDs
    const val PROD_ADMOB_APP_ID = "ca-app-pub-8155064094205693~8786482196"
    const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-8155064094205693/4272522119"
    const val PROD_NATIVE_ADVANCED_AD_UNIT_ID = "ca-app-pub-8155064094205693/4834940533"
    const val PROD_APP_OPEN_AD_UNIT_ID = "ca-app-pub-8155064094205693/5394032091"

    // Standard Google Test Ad Unit IDs (for development/testing)
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_NATIVE_ADVANCED_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
    const val TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"

    // Toggle for testing vs production
    var isTestMode: Boolean = false

    val interstitialAdUnitId: String
        get() = if (isTestMode) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID

    val nativeAdUnitId: String
        get() = if (isTestMode) TEST_NATIVE_ADVANCED_AD_UNIT_ID else PROD_NATIVE_ADVANCED_AD_UNIT_ID

    val appOpenAdUnitId: String
        get() = if (isTestMode) TEST_APP_OPEN_AD_UNIT_ID else PROD_APP_OPEN_AD_UNIT_ID

    // Frequency: show interstitial after every N eligible apply clicks
    const val APPLY_ACTIONS_BEFORE_INTERSTITIAL = 1

    // Cooldown duration between interstitials (in milliseconds)
    const val INTERSTITIAL_COOLDOWN_MS = 30_000L
}
