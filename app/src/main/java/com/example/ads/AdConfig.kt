package com.example.ads

object AdConfig {
    // Production AdMob IDs
    const val PROD_ADMOB_APP_ID = "ca-app-pub-8155064094205693~8786482196"

    // Native Advanced Ad Units
    const val PROD_NATIVE_UNIT_1 = "ca-app-pub-8155064094205693/4916038803" // Unit 1 (initial)
    const val PROD_NATIVE_UNIT_2 = "ca-app-pub-8155064094205693/9489708631" // Unit 2
    const val PROD_NATIVE_UNIT_3 = "ca-app-pub-8155064094205693/2665492013" // Unit 3
    const val PROD_NATIVE_UNIT_4 = "ca-app-pub-8155064094205693/9039328678" // Placement Unit
    const val PROD_NATIVE_UNIT_5 = "ca-app-pub-8155064094205693/9638094473" // Latest Unit

    // App Open Ad Unit
    const val PROD_APP_OPEN_AD_UNIT_ID = "ca-app-pub-8155064094205693/7941869421"

    // Standard Google Test Ad Unit IDs (for development/testing)
    const val TEST_NATIVE_ADVANCED_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
    const val TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-8155064094205693/4272522119"
    const val INTERSTITIAL_COOLDOWN_MS = 60_000L // 60 seconds

    // Toggle for testing vs production
    var isTestMode: Boolean = false

    val appOpenAdUnitId: String
        get() = if (isTestMode) TEST_APP_OPEN_AD_UNIT_ID else PROD_APP_OPEN_AD_UNIT_ID

    val interstitialAdUnitId: String
        get() = if (isTestMode) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID

    fun getNativeAdUnitId(index: Int = 0): String {
        if (isTestMode) return TEST_NATIVE_ADVANCED_AD_UNIT_ID
        return when (Math.floorMod(index, 5)) {
            0 -> PROD_NATIVE_UNIT_1
            1 -> PROD_NATIVE_UNIT_2
            2 -> PROD_NATIVE_UNIT_3
            3 -> PROD_NATIVE_UNIT_4
            else -> PROD_NATIVE_UNIT_5
        }
    }

    val nativeAdUnitId: String
        get() = getNativeAdUnitId(0)
}
