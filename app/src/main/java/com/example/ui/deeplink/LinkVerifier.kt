package com.example.ui.deeplink

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

/**
 * Helper to query domain verification status on API 31+ (Android 12+)
 * and provide browser fallback handling for Android App Links.
 */
object LinkVerifier {

    /**
     * Queries domain verification status on API 31+ (Android 12+).
     *
     * @param context Application or Activity context
     * @param host Target domain name (e.g., "jobsreport.online")
     * @return "verified", "unverified", or "unknown"
     */
    fun getVerificationStatus(context: Context, host: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val manager = context.getSystemService(android.content.pm.verify.domain.DomainVerificationManager::class.java)
                val userState = manager?.getDomainVerificationUserState(context.packageName)
                val state = userState?.hostToStateMap?.get(host)
                return when (state) {
                    android.content.pm.verify.domain.DomainVerificationUserState.DOMAIN_STATE_VERIFIED -> "verified"
                    android.content.pm.verify.domain.DomainVerificationUserState.DOMAIN_STATE_SELECTED,
                    android.content.pm.verify.domain.DomainVerificationUserState.DOMAIN_STATE_NONE -> "unverified"
                    else -> "unknown"
                }
            } catch (_: Throwable) {
                return "unknown"
            }
        }
        return "unknown"
    }

    /**
     * Fallback helper that fires ACTION_VIEW with CATEGORY_BROWSABLE to open
     * the given URL in the user's default web browser.
     */
    fun openInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Throwable) {
            // Safe fallback if no browser activity is available
        }
    }
}

/**
 * Top-level convenience function for [LinkVerifier.getVerificationStatus].
 */
fun getVerificationStatus(context: Context, host: String): String =
    LinkVerifier.getVerificationStatus(context, host)

/**
 * Top-level convenience function for [LinkVerifier.openInBrowser].
 */
fun openInBrowser(context: Context, url: String) =
    LinkVerifier.openInBrowser(context, url)
