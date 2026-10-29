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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import kotlinx.coroutines.delay

private fun dpToPx(context: Context, dp: Int): Int {
    return (dp * context.resources.displayMetrics.density).toInt()
}

// ============================================================
// 1. STORY REEL AD CARD (Horizontal Carousel: 115.dp x 210.dp)
// Same design as StoryReelCard
// ============================================================
@Composable
fun StoryReelAdCard(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.getNativeAdUnitId(0),
    slotKey: String = "story_reel_$adUnitId"
) {
    val context = LocalContext.current.applicationContext
    var nativeAd by remember(slotKey) {
        mutableStateOf(NativeAdManager.getCachedAd(slotKey))
    }
    var isFailed by remember(slotKey) { mutableStateOf(false) }

    LaunchedEffect(slotKey, adUnitId) {
        val cached = NativeAdManager.getCachedAd(slotKey)
        if (cached != null) {
            nativeAd = cached
            isFailed = false
        } else {
            NativeAdManager.loadAdForSlot(
                context = context,
                slotKey = slotKey,
                adUnitId = adUnitId,
                onAdReady = { ad ->
                    nativeAd = ad
                    isFailed = false
                },
                onAdFailed = {
                    isFailed = true
                }
            )
        }
    }

    if (nativeAd != null && !isFailed) {
        Box(
            modifier = modifier
                .width(115.dp)
                .height(210.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0B1120))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    createReelNativeAdView(ctx)
                },
                update = { view ->
                    val ad = nativeAd
                    if (ad != null && view.tag !== ad) {
                        view.tag = ad
                        populateReelNativeAdView(ad, view)
                    }
                }
            )
        }
    }
}

private fun createReelNativeAdView(context: Context): NativeAdView {
    val nativeAdView = NativeAdView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    val root = FrameLayout(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    }

    // 1. Full-bleed MediaView Background
    val mediaView = MediaView(context).apply {
        id = View.generateViewId()
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
            colors = intArrayOf(
                AndroidColor.parseColor("#1E1B4B"),
                AndroidColor.parseColor("#0F172A"),
                AndroidColor.parseColor("#020617")
            )
            orientation = android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM
        }
        background = bgDrawable
    }
    root.addView(mediaView)
    nativeAdView.mediaView = mediaView

    // 2. Story Vertical Gradient Scrim (identical to StoryReelCard)
    val scrimView = View(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        val scrimDrawable = android.graphics.drawable.GradientDrawable().apply {
            colors = intArrayOf(
                AndroidColor.parseColor("#66000000"), // 40% black at top
                AndroidColor.parseColor("#1A000000"), // 10% black in middle
                AndroidColor.parseColor("#E6000000")  // 90% dark black at bottom
            )
            orientation = android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM
        }
        background = scrimDrawable
    }
    root.addView(scrimView)

    // 3. Top Row: Avatar icon + "Sponsored" badge
    val topRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP
            setMargins(dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8), 0)
        }
        layoutParams = lp
    }

    val iconView = ImageView(context).apply {
        id = View.generateViewId()
        layoutParams = LinearLayout.LayoutParams(dpToPx(context, 28), dpToPx(context, 28)).apply {
            rightMargin = dpToPx(context, 6)
        }
        val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#2563EB"))
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setStroke(dpToPx(context, 1), AndroidColor.parseColor("#38BDF8"))
        }
        background = bgDrawable
        clipToOutline = true
        scaleType = ImageView.ScaleType.CENTER_CROP
    }
    topRow.addView(iconView)
    nativeAdView.iconView = iconView

    val badge = TextView(context).apply {
        text = "Sponsored"
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f)
        setTextColor(AndroidColor.parseColor("#38BDF8"))
        typeface = Typeface.MONOSPACE
        paint.isFakeBoldText = true
        val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#800B1120"))
            cornerRadius = dpToPx(context, 4).toFloat()
            setStroke(dpToPx(context, 1), AndroidColor.parseColor("#4D38BDF8"))
        }
        background = bgDrawable
        setPadding(dpToPx(context, 5), dpToPx(context, 2), dpToPx(context, 5), dpToPx(context, 2))
    }
    topRow.addView(badge)
    root.addView(topRow)

    // 4. Bottom Content: Headline + CTA Button
    val bottomLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM
            setMargins(dpToPx(context, 8), 0, dpToPx(context, 8), dpToPx(context, 10))
        }
        layoutParams = lp
    }

    val headlineView = TextView(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        setTextColor(AndroidColor.WHITE)
        typeface = Typeface.DEFAULT_BOLD
        paint.isFakeBoldText = true
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
        gravity = Gravity.START
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dpToPx(context, 6)
        }
    }
    bottomLayout.addView(headlineView)
    nativeAdView.headlineView = headlineView

    val ctaButton = Button(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
        setTextColor(AndroidColor.WHITE)
        typeface = Typeface.DEFAULT_BOLD
        isAllCaps = true
        val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#2563EB")) // PrimaryBlue
            cornerRadius = dpToPx(context, 10).toFloat()
        }
        background = bgDrawable
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dpToPx(context, 28)
        )
        setPadding(0, 0, 0, 0)
    }
    bottomLayout.addView(ctaButton)
    nativeAdView.callToActionView = ctaButton

    root.addView(bottomLayout)
    nativeAdView.addView(root)
    return nativeAdView
}

private fun populateReelNativeAdView(ad: NativeAd, view: NativeAdView) {
    (view.headlineView as? TextView)?.text = ad.headline

    val iconView = view.iconView as? ImageView
    if (ad.icon != null && ad.icon?.drawable != null) {
        iconView?.setImageDrawable(ad.icon?.drawable)
        iconView?.visibility = View.VISIBLE
    } else {
        iconView?.visibility = View.GONE
    }

    val mediaView = view.mediaView as? MediaView
    if (ad.mediaContent != null) {
        mediaView?.mediaContent = ad.mediaContent
        mediaView?.visibility = View.VISIBLE
    } else {
        mediaView?.visibility = View.VISIBLE
    }

    val cta = view.callToActionView as? Button
    if (!ad.callToAction.isNullOrBlank()) {
        cta?.text = ad.callToAction
        cta?.visibility = View.VISIBLE
    } else {
        cta?.text = "VIEW"
        cta?.visibility = View.VISIBLE
    }

    view.setNativeAd(ad)
}

// ============================================================
// 2. STORY GRID AD CARD (Main Feed Grid: aspectRatio 9/16)
// Same design as StoryGridCard
// ============================================================
@Composable
fun StoryGridAdCard(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.getNativeAdUnitId(1),
    slotKey: String = "story_grid_$adUnitId"
) {
    val context = LocalContext.current.applicationContext
    var nativeAd by remember(slotKey) {
        mutableStateOf(NativeAdManager.getCachedAd(slotKey))
    }
    var isFailed by remember(slotKey) { mutableStateOf(false) }

    LaunchedEffect(slotKey, adUnitId) {
        val cached = NativeAdManager.getCachedAd(slotKey)
        if (cached != null) {
            nativeAd = cached
            isFailed = false
        } else {
            NativeAdManager.loadAdForSlot(
                context = context,
                slotKey = slotKey,
                adUnitId = adUnitId,
                onAdReady = { ad ->
                    nativeAd = ad
                    isFailed = false
                },
                onAdFailed = {
                    isFailed = true
                }
            )
        }
    }

    if (nativeAd != null && !isFailed) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.02f))
                .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    createGridNativeAdView(ctx)
                },
                update = { view ->
                    val ad = nativeAd
                    if (ad != null && view.tag !== ad) {
                        view.tag = ad
                        populateGridNativeAdView(ad, view)
                    }
                }
            )
        }
    }
}

private fun createGridNativeAdView(context: Context): NativeAdView {
    val nativeAdView = NativeAdView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    val root = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dpToPx(context, 10), dpToPx(context, 10), dpToPx(context, 10), dpToPx(context, 10))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
    }

    // Top Header: Icon + Sponsored
    val header = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dpToPx(context, 8)
        }
    }

    val iconView = ImageView(context).apply {
        id = View.generateViewId()
        layoutParams = LinearLayout.LayoutParams(dpToPx(context, 30), dpToPx(context, 30)).apply {
            rightMargin = dpToPx(context, 8)
        }
        val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#1877F2"))
            shape = android.graphics.drawable.GradientDrawable.OVAL
        }
        background = bgDrawable
        clipToOutline = true
        scaleType = ImageView.ScaleType.CENTER_CROP
    }
    header.addView(iconView)
    nativeAdView.iconView = iconView

    val badge = TextView(context).apply {
        text = "SPONSORED"
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f)
        setTextColor(AndroidColor.parseColor("#38BDF8"))
        typeface = Typeface.MONOSPACE
        paint.isFakeBoldText = true
        val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#15233D"))
            cornerRadius = dpToPx(context, 4).toFloat()
            setStroke(dpToPx(context, 1), AndroidColor.parseColor("#3338BDF8"))
        }
        background = bgDrawable
        setPadding(dpToPx(context, 5), dpToPx(context, 2), dpToPx(context, 5), dpToPx(context, 2))
    }
    header.addView(badge)
    root.addView(header)

    // Media Content
    val mediaView = MediaView(context).apply {
        id = View.generateViewId()
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ).apply {
            bottomMargin = dpToPx(context, 8)
        }
        clipToOutline = true
        val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#0F172A"))
            cornerRadius = dpToPx(context, 10).toFloat()
        }
        background = bgDrawable
    }
    root.addView(mediaView)
    nativeAdView.mediaView = mediaView

    // Headline
    val headlineView = TextView(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTextColor(AndroidColor.WHITE)
        typeface = Typeface.DEFAULT_BOLD
        paint.isFakeBoldText = true
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dpToPx(context, 4)
        }
    }
    root.addView(headlineView)
    nativeAdView.headlineView = headlineView

    // Body
    val bodyView = TextView(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        setTextColor(AndroidColor.parseColor("#94A3B8"))
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dpToPx(context, 8)
        }
    }
    root.addView(bodyView)
    nativeAdView.bodyView = bodyView

    // CTA
    val ctaButton = Button(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        setTextColor(AndroidColor.WHITE)
        typeface = Typeface.DEFAULT_BOLD
        isAllCaps = true
        val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#1877F2"))
            cornerRadius = dpToPx(context, 8).toFloat()
        }
        background = bgDrawable
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dpToPx(context, 32)
        )
    }
    root.addView(ctaButton)
    nativeAdView.callToActionView = ctaButton

    nativeAdView.addView(root)
    return nativeAdView
}

private fun populateGridNativeAdView(ad: NativeAd, view: NativeAdView) {
    (view.headlineView as? TextView)?.text = ad.headline
    (view.bodyView as? TextView)?.text = ad.body

    val iconView = view.iconView as? ImageView
    if (ad.icon != null && ad.icon?.drawable != null) {
        iconView?.setImageDrawable(ad.icon?.drawable)
        iconView?.visibility = View.VISIBLE
    } else {
        iconView?.visibility = View.GONE
    }

    val mediaView = view.mediaView as? MediaView
    if (ad.mediaContent != null) {
        mediaView?.mediaContent = ad.mediaContent
        mediaView?.visibility = View.VISIBLE
    } else {
        mediaView?.visibility = View.GONE
    }

    val cta = view.callToActionView as? Button
    if (!ad.callToAction.isNullOrBlank()) {
        cta?.text = ad.callToAction
        cta?.visibility = View.VISIBLE
    } else {
        cta?.text = "LEARN MORE"
        cta?.visibility = View.VISIBLE
    }

    view.setNativeAd(ad)
}

// ============================================================
// 3. FULL SCREEN STORY AD VIEWER (Shown after every 5 stories)
// Same immersive design as viewed stories
// ============================================================
@Composable
fun FullScreenStoryAdViewer(
    adUnitId: String = AdConfig.getNativeAdUnitId(2),
    slotKey: String = "fullscreen_story_ad",
    onClose: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    var nativeAd by remember(slotKey) {
        mutableStateOf(NativeAdManager.getCachedAd(slotKey))
    }
    var isFailed by remember(slotKey) { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(slotKey, adUnitId) {
        val cached = NativeAdManager.getCachedAd(slotKey)
        if (cached != null) {
            nativeAd = cached
            isFailed = false
        } else {
            NativeAdManager.loadAdForSlot(
                context = context,
                slotKey = slotKey,
                adUnitId = adUnitId,
                onAdReady = { ad ->
                    nativeAd = ad
                    isFailed = false
                },
                onAdFailed = {
                    isFailed = true
                    // If ad failed, automatically advance to next story
                    onNext()
                }
            )
        }
    }

    // 5-second story auto-advance timer
    LaunchedEffect(isPaused, nativeAd) {
        if (!isPaused && nativeAd != null) {
            val stepMs = 50L
            val totalSteps = 5000L / stepMs
            while (progress < 1f && !isPaused) {
                delay(stepMs)
                progress += (1f / totalSteps)
            }
            if (progress >= 1f) {
                onNext()
            }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF050B18))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPaused = true
                            tryAwaitRelease()
                            isPaused = false
                        },
                        onTap = { offset ->
                            val width = size.width
                            if (offset.x < width * 0.35f) onPrev()
                            else if (offset.x > width * 0.65f) onNext()
                            else isPaused = !isPaused
                        }
                    )
                }
        ) {
            // Native Ad content or loading container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0B1120),
                                Color(0xFF0F172A),
                                Color(0xFF050B18)
                            )
                        )
                    )
            ) {
                if (nativeAd != null) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            createFullScreenNativeAdView(ctx)
                        },
                        update = { view ->
                            val ad = nativeAd
                            if (ad != null && view.tag !== ad) {
                                view.tag = ad
                                populateFullScreenNativeAdView(ad, view)
                            }
                        }
                    )
                }
            }

            // Top Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 12.dp, end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(Color.White)
                    )
                }
            }

            // Header Bar: "SPONSORED STORY" + Close (X) button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, start = 14.dp, end = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF15233D))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "SPONSORED",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Suggested Content",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun createFullScreenNativeAdView(context: Context): NativeAdView {
    val nativeAdView = NativeAdView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    val root = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dpToPx(context, 20), dpToPx(context, 70), dpToPx(context, 20), dpToPx(context, 40))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
    }

    // Media Content area (large immersion)
    val mediaView = MediaView(context).apply {
        id = View.generateViewId()
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ).apply {
            bottomMargin = dpToPx(context, 16)
        }
        clipToOutline = true
        val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#0F172A"))
            cornerRadius = dpToPx(context, 16).toFloat()
        }
        background = bgDrawable
    }
    root.addView(mediaView)
    nativeAdView.mediaView = mediaView

    // Advertiser Info Row
    val authorRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dpToPx(context, 10)
        }
    }

    val iconView = ImageView(context).apply {
        id = View.generateViewId()
        layoutParams = LinearLayout.LayoutParams(dpToPx(context, 44), dpToPx(context, 44)).apply {
            rightMargin = dpToPx(context, 12)
        }
        val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#1877F2"))
            shape = android.graphics.drawable.GradientDrawable.OVAL
        }
        background = bgDrawable
        clipToOutline = true
        scaleType = ImageView.ScaleType.CENTER_CROP
    }
    authorRow.addView(iconView)
    nativeAdView.iconView = iconView

    val textCol = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

    val advertiserView = TextView(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setTextColor(AndroidColor.WHITE)
        typeface = Typeface.DEFAULT_BOLD
        paint.isFakeBoldText = true
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    textCol.addView(advertiserView)
    nativeAdView.advertiserView = advertiserView

    val headlineView = TextView(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTextColor(AndroidColor.parseColor("#94A3B8"))
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
    }
    textCol.addView(headlineView)
    nativeAdView.headlineView = headlineView

    authorRow.addView(textCol)
    root.addView(authorRow)

    // Body
    val bodyView = TextView(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        setTextColor(AndroidColor.parseColor("#E2E8F0"))
        maxLines = 3
        ellipsize = TextUtils.TruncateAt.END
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dpToPx(context, 16)
        }
    }
    root.addView(bodyView)
    nativeAdView.bodyView = bodyView

    // Full Width CTA Button
    val ctaButton = Button(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setTextColor(AndroidColor.WHITE)
        typeface = Typeface.DEFAULT_BOLD
        isAllCaps = true
        val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#1877F2"))
            cornerRadius = dpToPx(context, 12).toFloat()
        }
        background = bgDrawable
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dpToPx(context, 48)
        )
    }
    root.addView(ctaButton)
    nativeAdView.callToActionView = ctaButton

    nativeAdView.addView(root)
    return nativeAdView
}

private fun populateFullScreenNativeAdView(ad: NativeAd, view: NativeAdView) {
    (view.advertiserView as? TextView)?.text = ad.advertiser ?: "Promoted Story"
    (view.headlineView as? TextView)?.text = ad.headline
    (view.bodyView as? TextView)?.text = ad.body

    val iconView = view.iconView as? ImageView
    if (ad.icon != null && ad.icon?.drawable != null) {
        iconView?.setImageDrawable(ad.icon?.drawable)
        iconView?.visibility = View.VISIBLE
    } else {
        iconView?.visibility = View.GONE
    }

    val mediaView = view.mediaView as? MediaView
    if (ad.mediaContent != null) {
        mediaView?.mediaContent = ad.mediaContent
        mediaView?.visibility = View.VISIBLE
    } else {
        mediaView?.visibility = View.GONE
    }

    val cta = view.callToActionView as? Button
    if (!ad.callToAction.isNullOrBlank()) {
        cta?.text = ad.callToAction
        cta?.visibility = View.VISIBLE
    } else {
        cta?.text = "LEARN MORE"
        cta?.visibility = View.VISIBLE
    }

    view.setNativeAd(ad)
}
