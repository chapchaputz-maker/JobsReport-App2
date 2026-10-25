package com.example.ui.screens

import com.example.ads.NativeAdCard
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import android.widget.Toast
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.JobOpportunity
import com.example.data.isJobExpired
import com.example.state.AppViewModel
import com.example.state.NavRoute
import com.example.ui.components.JobsPagination
import com.example.ui.theme.AmberYellow
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BorderWhite10
import com.example.ui.theme.BorderWhite5
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.ceil

private const val CATEGORY_PAGE_SIZE = 10

@Composable
fun CategoryScreen(viewModel: AppViewModel) {
    val categoryName = viewModel.selectedCategoryName ?: "General"
    val countryName = viewModel.selectedCountry
    val isWorldwide = countryName.equals("Worldwide", ignoreCase = true)
    var currentPage by remember { mutableIntStateOf(1) }

    // Filter category jobs by country if specific country selected
    val filteredJobs = remember(viewModel.categoryJobs, countryName, isWorldwide) {
        if (isWorldwide) {
            viewModel.categoryJobs
        } else {
            val matched = viewModel.categoryJobs.filter { job ->
                val loc = (job.location ?: "").lowercase()
                val ctry = (job.country ?: "").lowercase()
                val target = countryName.lowercase()
                loc.contains(target) || ctry.contains(target)
            }
            if (matched.isNotEmpty()) matched else viewModel.categoryJobs
        }
    }

    val totalPages = remember(filteredJobs.size) {
        val pages = ceil(filteredJobs.size.toDouble() / CATEGORY_PAGE_SIZE).toInt()
        if (pages < 1) 1 else pages
    }

    val paginatedJobs = remember(filteredJobs, currentPage) {
        val startIndex = (currentPage - 1) * CATEGORY_PAGE_SIZE
        if (startIndex >= filteredJobs.size) {
            emptyList()
        } else {
            filteredJobs.subList(
                startIndex,
                minOf(startIndex + CATEGORY_PAGE_SIZE, filteredJobs.size)
            )
        }
    }

    val scrollState = rememberScrollState()

    androidx.compose.runtime.LaunchedEffect(currentPage) {
        scrollState.animateScrollTo(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. Back Navigation Button ---
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { viewModel.navigateTo(NavRoute.INTELLIGENCE_FEED) }
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Home",
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "HOME",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        // --- 2. Title & Header Info ---
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (isWorldwide) {
                    "$categoryName Jobs"
                } else {
                    "$categoryName Jobs in $countryName"
                }.uppercase(),
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )

            Text(
                text = if (isWorldwide) {
                    "Browse ${filteredJobs.size} ${categoryName.lowercase()} jobs worldwide"
                } else {
                    "Browse ${filteredJobs.size} ${categoryName.lowercase()} jobs in $countryName"
                },
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // --- 3. Content Body ---
        if (viewModel.isCategoryJobsLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LightBlue)
            }
        } else if (filteredJobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "No Jobs",
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "No jobs found",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isWorldwide) {
                            "No ${categoryName.lowercase()} jobs available right now."
                        } else {
                            "No ${categoryName.lowercase()} jobs available in $countryName."
                        },
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            val context = LocalContext.current
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                paginatedJobs.forEachIndexed { index, job ->
                    val isSaved = viewModel.isJobSaved(job.id)
                    CategoryJobCard(
                        job = job,
                        isSaved = isSaved,
                        onToggleSave = {
                            val nowSaved = viewModel.toggleSaveJob(job)
                            Toast.makeText(
                                context,
                                if (nowSaved) "Job saved to Bookmarks" else "Job removed from Bookmarks",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onClick = { viewModel.selectJob(job) }
                    )
                    if ((index + 1) % 3 == 0) {
                        NativeAdCard()
                    }
                }
            }

            // Pagination Controls
            JobsPagination(
                currentPage = currentPage,
                totalPages = totalPages,
                totalJobs = filteredJobs.size,
                onPageChange = { currentPage = it },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun CategoryJobCard(
    job: JobOpportunity,
    isSaved: Boolean = false,
    onToggleSave: () -> Unit = {},
    onClick: () -> Unit
) {
    val isExpired = job.isJobExpired()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isExpired) BgCard.copy(alpha = 0.6f) else BgCard)
            .border(1.dp, if (isExpired) BorderWhite5 else BorderWhite10, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Row 1: Logo + (Title & Company) + Bookmark Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Logo or Fallback Monogram
                if (!job.logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = job.logoUrl,
                        contentDescription = job.company,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BgDeep)
                            .border(1.dp, BorderWhite10, RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryBlue.copy(alpha = 0.2f))
                            .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = job.company.take(1).uppercase(),
                            color = LightBlue,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Title + Company Name
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = job.title,
                        color = if (isExpired) TextSecondary else TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = job.company,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bookmark action
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isSaved) "Remove from Saved Jobs" else "Save Job",
                        tint = if (isSaved) AmberYellow else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Row 2: Role / Category Badge & Post time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!job.role.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isExpired) DangerRed.copy(alpha = 0.15f) else PrimaryBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isExpired) "EXPIRED" else job.role.uppercase(),
                            color = if (isExpired) DangerRed else LightBlue,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (!job.postedAt.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = TextMuted,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = job.postedAt,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderWhite5)
            )

            // Row 3: Location (Left) + Salary or Signal ID (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = job.location ?: "Remote",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!job.salary.isNullOrBlank()) {
                    Text(
                        text = job.salary,
                        color = EmeraldGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "JR-${job.id.takeLast(4).uppercase()}",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
