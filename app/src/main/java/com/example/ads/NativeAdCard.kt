package com.example.ads

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.BgCard
import com.example.ui.theme.BorderWhite10
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun NativeAdCard(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.nativeAdUnitId
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isFailedToLoad by remember { mutableStateOf(false) }

    LaunchedEffect(adUnitId) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                nativeAd?.destroy()
                nativeAd = ad
                isFailedToLoad = false
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isFailedToLoad = true
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    DisposableEffect(Unit) {
        onDispose {
            nativeAd?.destroy()
        }
    }

    AnimatedVisibility(
        visible = nativeAd != null && !isFailedToLoad,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BgCard)
                .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    createNativeAdView(ctx)
                },
                update = { nativeAdView ->
                    val ad = nativeAd
                    if (ad != null) {
                        populateNativeAdView(ad, nativeAdView)
                    }
                }
            )
        }
    }
}

private fun createNativeAdView(context: Context): NativeAdView {
    val nativeAdView = NativeAdView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    val rootLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // --- Row 1: Header (Badge + Advertiser / Store) ---
    val headerRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dpToPx(context, 8)
        }
    }

    // "SPONSORED" Badge
    val adBadge = TextView(context).apply {
        text = "SPONSORED"
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
        setTextColor(AndroidColor.parseColor("#38BDF8")) // LightBlue
        typeface = Typeface.MONOSPACE
        paint.isFakeBoldText = true
        val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#15233D"))
            cornerRadius = dpToPx(context, 4).toFloat()
            setStroke(dpToPx(context, 1), AndroidColor.parseColor("#3338BDF8"))
        }
        background = bgDrawable
        setPadding(dpToPx(context, 6), dpToPx(context, 2), dpToPx(context, 6), dpToPx(context, 2))
    }
    headerRow.addView(adBadge)

    // Advertiser Label
    val advertiserView = TextView(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        setTextColor(AndroidColor.parseColor("#94A3B8")) // TextSecondary
        typeface = Typeface.MONOSPACE
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        setPadding(dpToPx(context, 8), 0, 0, 0)
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }
    headerRow.addView(advertiserView)
    nativeAdView.advertiserView = advertiserView

    rootLayout.addView(headerRow)

    // --- Row 2: Icon + Headline + Body ---
    val contentRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.TOP
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dpToPx(context, 10)
        }
    }

    // Ad Icon
    val iconView = ImageView(context).apply {
        id = View.generateViewId()
        layoutParams = LinearLayout.LayoutParams(dpToPx(context, 48), dpToPx(context, 48)).apply {
            rightMargin = dpToPx(context, 12)
        }
        val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#090D16"))
            cornerRadius = dpToPx(context, 12).toFloat()
            setStroke(dpToPx(context, 1), AndroidColor.parseColor("#26FFFFFF"))
        }
        background = bgDrawable
        clipToOutline = true
        scaleType = ImageView.ScaleType.CENTER_CROP
    }
    contentRow.addView(iconView)
    nativeAdView.iconView = iconView

    // Text Column (Headline & Body)
    val textCol = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

    val headlineView = TextView(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setTextColor(AndroidColor.parseColor("#F8FAFC")) // TextPrimary
        paint.isFakeBoldText = true
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
    }
    textCol.addView(headlineView)
    nativeAdView.headlineView = headlineView

    val bodyView = TextView(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTextColor(AndroidColor.parseColor("#94A3B8")) // TextSecondary
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
        setPadding(0, dpToPx(context, 3), 0, 0)
    }
    textCol.addView(bodyView)
    nativeAdView.bodyView = bodyView

    contentRow.addView(textCol)
    rootLayout.addView(contentRow)

    // Optional Media View (for ads that include media)
    val mediaView = MediaView(context).apply {
        id = View.generateViewId()
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dpToPx(context, 140)
        ).apply {
            bottomMargin = dpToPx(context, 10)
        }
        visibility = View.GONE
        clipToOutline = true
        val mediaBg = android.graphics.drawable.GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#090D16"))
            cornerRadius = dpToPx(context, 12).toFloat()
        }
        background = mediaBg
    }
    rootLayout.addView(mediaView)
    nativeAdView.mediaView = mediaView

    // --- Row 3: Call To Action Button ---
    val ctaButton = Button(context).apply {
        id = View.generateViewId()
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dpToPx(context, 40)
        )
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTextColor(AndroidColor.WHITE)
        typeface = Typeface.MONOSPACE
        paint.isFakeBoldText = true
        isAllCaps = true
        val btnDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#0284C7")) // PrimaryBlue
            cornerRadius = dpToPx(context, 10).toFloat()
        }
        background = btnDrawable
    }
    rootLayout.addView(ctaButton)
    nativeAdView.callToActionView = ctaButton

    nativeAdView.addView(rootLayout)
    return nativeAdView
}

private fun populateNativeAdView(nativeAd: NativeAd, nativeAdView: NativeAdView) {
    // Headline
    (nativeAdView.headlineView as? TextView)?.text = nativeAd.headline

    // Body
    val body = nativeAd.body
    if (body != null) {
        nativeAdView.bodyView?.visibility = View.VISIBLE
        (nativeAdView.bodyView as? TextView)?.text = body
    } else {
        nativeAdView.bodyView?.visibility = View.GONE
    }

    // Advertiser or Store
    val advertiser = nativeAd.advertiser ?: nativeAd.store
    if (advertiser != null) {
        nativeAdView.advertiserView?.visibility = View.VISIBLE
        (nativeAdView.advertiserView as? TextView)?.text = advertiser
    } else {
        nativeAdView.advertiserView?.visibility = View.GONE
    }

    // Icon
    val icon = nativeAd.icon
    if (icon != null) {
        nativeAdView.iconView?.visibility = View.VISIBLE
        (nativeAdView.iconView as? ImageView)?.setImageDrawable(icon.drawable)
    } else {
        nativeAdView.iconView?.visibility = View.GONE
    }

    // Media View
    val mediaContent = nativeAd.mediaContent
    if (mediaContent != null && mediaContent.hasVideoContent()) {
        nativeAdView.mediaView?.visibility = View.VISIBLE
        nativeAdView.mediaView?.mediaContent = mediaContent
    } else {
        nativeAdView.mediaView?.visibility = View.GONE
    }

    // Call to action
    val cta = nativeAd.callToAction
    if (cta != null) {
        nativeAdView.callToActionView?.visibility = View.VISIBLE
        (nativeAdView.callToActionView as? Button)?.text = cta
    } else {
        nativeAdView.callToActionView?.visibility = View.GONE
    }

    nativeAdView.setNativeAd(nativeAd)
}

private fun dpToPx(context: Context, dp: Int): Int {
    val density = context.resources.displayMetrics.density
    return (dp * density).toInt()
}
