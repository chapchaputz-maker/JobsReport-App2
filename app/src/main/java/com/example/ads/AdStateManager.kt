package com.example.ads

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Coordinates global display state so App Open and Interstitial ads
 * never show simultaneously or conflict with each other.
 */
object AdStateManager {
    private val interstitialShowing = AtomicBoolean(false)
    private val appOpenShowing = AtomicBoolean(false)

    val isInterstitialShowing: Boolean
        get() = interstitialShowing.get()

    val isAppOpenShowing: Boolean
        get() = appOpenShowing.get()

    fun setInterstitialShowing(showing: Boolean) {
        interstitialShowing.set(showing)
    }

    fun setAppOpenShowing(showing: Boolean) {
        appOpenShowing.set(showing)
    }

    fun canShowAppOpen(): Boolean {
        return !interstitialShowing.get() && !appOpenShowing.get()
    }

    fun canShowInterstitial(): Boolean {
        return !interstitialShowing.get() && !appOpenShowing.get()
    }
}
