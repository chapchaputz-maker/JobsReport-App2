package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import com.example.ads.InterstitialAdManager
import com.example.ads.NativeAdCard
import com.example.data.isJobExpired
import com.example.data.resolveApplicationUrl
import android.text.Html
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.JobAttachment
import com.example.data.JobOpportunity
import com.example.state.AppViewModel
import com.example.state.NavRoute
import com.example.ui.theme.AmberYellow
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BgSurface
import com.example.ui.theme.BorderWhite10
import com.example.ui.theme.BorderWhite5
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobDetailScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val job = viewModel.selectedJob

    // Fallback if no job selected
    if (job == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDeep)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Not found",
                    tint = DangerRed,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Job Signal Not Found",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "This listing may have been removed or expired. Browse active market signals.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Button(
                    onClick = { viewModel.navigateTo(NavRoute.ALL_JOBS) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("← Back to All Jobs")
                }
            }
        }
        return
    }

    val isExpired = job.isJobExpired()

    // Image/Attachment Viewer Dialog State
    var viewerOpen by remember { mutableStateOf(false) }
    var viewerIndex by remember { mutableIntStateOf(0) }
    val attachments = job.images ?: emptyList()

    // Related Jobs (excluding current)
    val relatedJobs = remember(job.id, viewModel.activeJobs) {
        viewModel.activeJobs.filter { it.id != job.id }.take(6)
    }

    val scrollState = rememberScrollState()

    // Ensure screen always resets to the top when navigating or selecting a related job
    LaunchedEffect(job.id) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .verticalScroll(scrollState)
    ) {
        // --- 1. Top Header Sticky Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSurface.copy(alpha = 0.95f))
                .border(1.dp, BorderWhite10)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.navigateTo(NavRoute.ALL_JOBS) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ALL JOBS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isExpired) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(DangerRed.copy(alpha = 0.15f))
                            .border(1.dp, DangerRed.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "EXPIRED",
                            color = DangerRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                val isSaved = viewModel.isJobSaved(job.id)
                IconButton(
                    onClick = {
                        val nowSaved = viewModel.toggleSaveJob(job)
                        Toast.makeText(
                            context,
                            if (nowSaved) "Job saved to Bookmarks" else "Job removed from Bookmarks",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isSaved) "Remove from Saved Jobs" else "Save Job",
                        tint = if (isSaved) AmberYellow else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${job.title} at ${job.company} - Check out on JobsReport!"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Job"))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 2. Primary Job Overview Header ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Company Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!job.logoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = job.logoUrl,
                                contentDescription = job.company,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BgDeep),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(PrimaryBlue, LightBlue)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = job.company.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = job.company,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (!job.companyWebsite.isNullOrBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(job.companyWebsite))
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = "Website",
                                        tint = LightBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = job.companyWebsite.removePrefix("https://").removePrefix("http://"),
                                        color = LightBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Launch,
                                        contentDescription = "Open",
                                        tint = LightBlue,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Verified Employer Partner",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = job.title,
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 26.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isExpired) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(DangerRed.copy(alpha = 0.1f))
                                    .border(1.dp, DangerRed.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Expired",
                                    color = DangerRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (isExpired) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DangerRed.copy(alpha = 0.05f))
                                .border(1.dp, DangerRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Expired",
                                tint = DangerRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "This job listing has expired. Applications are no longer accepted. Browse related jobs below.",
                                color = DangerRed.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Key Badges Grid
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailBadge(
                            icon = Icons.Default.LocationOn,
                            text = job.location ?: "Remote",
                            tint = TextSecondary
                        )

                        if (!job.salary.isNullOrBlank()) {
                            DetailBadge(
                                icon = null,
                                text = "💰 ${job.salary}",
                                tint = EmeraldGreen
                            )
                        }

                        DetailBadge(
                            icon = Icons.Default.Work,
                            text = job.role ?: "General",
                            tint = LightBlue
                        )

                        DetailBadge(
                            icon = Icons.Default.CalendarMonth,
                            text = "Posted: ${job.postedAt ?: "Recent"}",
                            tint = TextMuted
                        )

                        if (!job.expiresAt.isNullOrBlank()) {
                            DetailBadge(
                                icon = Icons.Default.Schedule,
                                text = if (isExpired) "Expired: ${job.expiresAt}" else "Expires: ${job.expiresAt}",
                                tint = if (isExpired) DangerRed else AmberYellow
                            )
                        }
                    }
                }
            }

            // --- Native Ad 1: Before Job Details ---
            NativeAdCard()

            // --- 3. Action / Application Instructions Bar ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!job.applicationInstructions.isNullOrBlank() && !isExpired) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryBlue.copy(alpha = 0.1f))
                                .border(1.dp, PrimaryBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "📋 HOW TO APPLY:",
                                    color = LightBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = job.applicationInstructions,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    if (!job.whatsappNumber.isNullOrBlank() && !isExpired) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF25D366))
                                .clickable {
                                    val cleaned = job.whatsappNumber.replace(Regex("[^0-9]"), "")
                                    val text = Uri.encode("Hello, I am interested in the ${job.title} position at ${job.company}.")
                                    val waUrl = "https://wa.me/$cleaned?text=$text"
                                    val activity = context.findActivity()
                                    if (activity != null) {
                                        InterstitialAdManager.handleApplyAction(activity) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                                            context.startActivity(intent)
                                        }
                                    } else {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                                        context.startActivity(intent)
                                    }
                                }
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "WhatsApp",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "APPLY VIA WHATSAPP",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    } else if (!isExpired) {
                        val applyUrl = job.resolveApplicationUrl()
                        val isEmail = applyUrl.startsWith("mailto:", ignoreCase = true)
                        val isCareerRedirect = applyUrl.contains("careerredirect", ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(PrimaryBlue, LightBlue)
                                    )
                                )
                                .clickable {
                                    val activity = context.findActivity()
                                    if (activity != null) {
                                        InterstitialAdManager.handleApplyAction(activity) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(applyUrl))
                                            context.startActivity(intent)
                                        }
                                    } else {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(applyUrl))
                                        context.startActivity(intent)
                                    }
                                }
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when {
                                        isEmail -> "✉️ SEND APPLICATION EMAIL"
                                        isCareerRedirect -> "🚀 APPLY NOW (CAREER REDIRECT)"
                                        else -> "APPLY NOW ON PORTAL"
                                    },
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Launch,
                                    contentDescription = "Apply",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(BgDeep)
                                .border(1.dp, BorderWhite5, RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isExpired) "🚫 APPLICATION CLOSED" else "NO ONLINE LINK AVAILABLE",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Save / Bookmark Action Button
                    val isJobSaved = viewModel.isJobSaved(job.id)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isJobSaved) AmberYellow.copy(alpha = 0.15f) else BgDeep)
                            .border(
                                1.dp,
                                if (isJobSaved) AmberYellow.copy(alpha = 0.4f) else BorderWhite10,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                val nowSaved = viewModel.toggleSaveJob(job)
                                Toast.makeText(
                                    context,
                                    if (nowSaved) "Job saved to Bookmarks" else "Job removed from Bookmarks",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isJobSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isJobSaved) "Saved" else "Save Job",
                                tint = if (isJobSaved) AmberYellow else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isJobSaved) "SAVED IN BOOKMARKS" else "SAVE JOB FOR LATER",
                                color = if (isJobSaved) AmberYellow else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }

            // --- 4. Safety Alert Banner ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DangerRed.copy(alpha = 0.05f))
                    .border(1.dp, DangerRed.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Stay safe",
                            tint = DangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stay safe: Never pay for job applications or employment offers.",
                            color = DangerRed.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DangerRed.copy(alpha = 0.15f))
                            .border(1.dp, DangerRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .clickable {
                                val mailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:jjovinatha@gmail.com?subject=Report%20Job%20${job.id}"))
                                context.startActivity(mailIntent)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Report",
                                tint = DangerRed,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "REPORT",
                                color = DangerRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // --- 5. Attachments Section (if available) ---
            if (attachments.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgCard)
                        .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Attachments",
                                tint = LightBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ATTACHMENTS (${attachments.size})",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            attachments.forEachIndexed { index, att ->
                                val isPdf = att.type == "pdf" || att.name?.endsWith(".pdf", ignoreCase = true) == true
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BgDeep)
                                        .border(1.dp, BorderWhite10, RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewerIndex = index
                                            viewerOpen = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPdf) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(4.dp)
                                        ) {
                                            Text(
                                                text = "PDF",
                                                color = DangerRed,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = att.name ?: "Document.pdf",
                                                color = TextMuted,
                                                fontSize = 8.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    } else {
                                        AsyncImage(
                                            model = att.thumbnail ?: att.url,
                                            contentDescription = att.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 6. Job Description Section ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Description",
                            tint = LightBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "JOB DESCRIPTION",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (!job.description.isNullOrBlank()) {
                        AndroidView(
                            factory = { ctx ->
                                TextView(ctx).apply {
                                    setTextColor(android.graphics.Color.WHITE)
                                    textSize = 13f
                                    setLineSpacing(12f, 1f)
                                }
                            },
                            update = { textView ->
                                textView.text = Html.fromHtml(job.description, Html.FROM_HTML_MODE_COMPACT)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "No detailed description provided for this signal listing.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // --- 7. Key Job Details Metadata Table ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SPECIFICATIONS & METADATA",
                        color = LightBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    val metaList = listOf(
                        "Status" to if (isExpired) "Expired" else "Active",
                        "Company" to job.company,
                        "Location" to (job.location ?: "Remote"),
                        "Role Category" to (job.role ?: "General"),
                        "Salary" to (job.salary ?: "Not specified"),
                        "Category" to (job.jobCategory ?: "Technology"),
                        "Employment Type" to (job.employmentType ?: "FULL_TIME"),
                        "Workplace" to (job.workplaceType ?: "Onsite"),
                        "Education" to (job.educationLevel ?: "Degree / Diploma"),
                        "Experience" to if ((job.experienceMonths ?: 0) > 0) "${job.experienceMonths} months" else "Not specified",
                        "Posted Date" to (job.postedAt ?: "Recent"),
                        "Signal Identifier" to "JR-${job.id.takeLast(8).uppercase()}"
                    )

                    metaList.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderWhite5, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = value,
                                color = if (label == "Status") (if (isExpired) DangerRed else EmeraldGreen) else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- Native Ad 2: About Company ---
            NativeAdCard()

            // --- 8. About Company & Related Jobs ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Company",
                            tint = LightBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ABOUT ${job.company.uppercase()}",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgDeep)
                            .border(1.dp, BorderWhite5, RoundedCornerShape(12.dp))
                            .clickable {
                                val found = viewModel.companies.find { it.name.contains(job.company, ignoreCase = true) || job.company.contains(it.name, ignoreCase = true) }
                                    ?: com.example.data.Company(
                                        id = job.company.lowercase().replace(" ", "-"),
                                        name = job.company,
                                        url = job.companyWebsite,
                                        logoUrl = job.logoUrl,
                                        description = "Employer profile and career signals for ${job.company}."
                                    )
                                viewModel.selectCompany(found)
                                viewModel.navigateTo(NavRoute.COMPANIES)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "View all jobs at ${job.company}",
                            color = LightBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Launch,
                            contentDescription = "View",
                            tint = LightBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (relatedJobs.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = "Related",
                            tint = LightBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isExpired) "Similar Active Jobs" else "Related Jobs",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = if (isExpired) "This job has expired. Here are similar active opportunities." else "Explore more ${job.role ?: ""} jobs similar to this one.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    relatedJobs.forEach { rj ->
                        val rjExpired = rj.isJobExpired()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (rjExpired) BgCard.copy(alpha = 0.5f) else BgCard)
                                .border(1.dp, if (rjExpired) BorderWhite5 else BorderWhite10, RoundedCornerShape(16.dp))
                                .clickable { viewModel.selectJob(rj) }
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = rj.title,
                                        color = if (rjExpired) TextMuted else TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (rjExpired) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(DangerRed.copy(alpha = 0.1f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Expired",
                                                color = DangerRed,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = rj.company,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                    if (!rj.salary.isNullOrBlank()) {
                                        Text(
                                            text = rj.salary,
                                            color = EmeraldGreen,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- Native Ad 3: End of Page ---
            NativeAdCard()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // --- Fullscreen Attachment Viewer Modal ---
    if (viewerOpen && attachments.isNotEmpty()) {
        Dialog(
            onDismissRequest = { viewerOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val currentAtt = attachments.getOrNull(viewerIndex)

                // Modal Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${viewerIndex + 1} / ${attachments.size} • ${currentAtt?.name ?: "Attachment"}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row {
                        IconButton(onClick = {
                            currentAtt?.url?.let { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(onClick = { viewerOpen = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Image / PDF Content Preview
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentAtt != null) {
                        AsyncImage(
                            model = currentAtt.url,
                            contentDescription = currentAtt.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Nav Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 24.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (viewerIndex > 0) viewerIndex-- },
                        enabled = viewerIndex > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Prev",
                            tint = if (viewerIndex > 0) Color.White else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        onClick = { if (viewerIndex < attachments.size - 1) viewerIndex++ },
                        enabled = viewerIndex < attachments.size - 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next",
                            tint = if (viewerIndex < attachments.size - 1) Color.White else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    text: String,
    tint: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BgDeep)
            .border(1.dp, BorderWhite5, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = tint,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = tint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

