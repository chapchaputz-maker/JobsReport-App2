package com.example.ui.screens

import com.example.ads.NativeAdCard
import com.example.data.isJobExpired
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import android.widget.Toast
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.data.JobOpportunity
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
import com.example.ui.theme.VioletPurple

const val JOBS_PER_PAGE = 15

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobsListScreen(viewModel: AppViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("All") }
    var currentPage by remember { mutableIntStateOf(1) }

    // Derive roles list from available jobs
    val allRoles = remember(viewModel.activeJobs) {
        listOf("All") + viewModel.activeJobs.mapNotNull { it.role }.filter { it.isNotBlank() }.distinct()
    }

    // Filter jobs based on search query, selected role, and selected country
    val filteredJobs = viewModel.activeJobs.filter { job ->
        val matchesSearch = searchQuery.isBlank() ||
                job.title.contains(searchQuery, ignoreCase = true) ||
                job.company.contains(searchQuery, ignoreCase = true) ||
                (job.location?.contains(searchQuery, ignoreCase = true) == true)
        val matchesRole = selectedRole == "All" || job.role.equals(selectedRole, ignoreCase = true)
        val matchesCountry = viewModel.selectedCountry.equals("Worldwide", ignoreCase = true) ||
                viewModel.selectedCountry.isBlank() ||
                (job.country?.contains(viewModel.selectedCountry, ignoreCase = true) == true) ||
                (job.location?.contains(viewModel.selectedCountry, ignoreCase = true) == true) ||
                (job.company.contains(viewModel.selectedCountry, ignoreCase = true))

        matchesSearch && matchesRole && matchesCountry
    }

    val totalActiveJobs = filteredJobs.size
    val uniqueCompanies = filteredJobs.map { it.company }.distinct().size
    val uniqueRoles = filteredJobs.mapNotNull { it.role }.distinct().size
    val totalPages = maxOf(1, (totalActiveJobs + JOBS_PER_PAGE - 1) / JOBS_PER_PAGE)

    val paginatedJobs = filteredJobs.drop((currentPage - 1) * JOBS_PER_PAGE).take(JOBS_PER_PAGE)

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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Page Header ---
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Trending",
                    tint = LightBlue,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (viewModel.selectedCountry.equals("Worldwide", ignoreCase = true)) {
                        "GLOBAL MARKET TELEMETRY"
                    } else {
                        "${viewModel.selectedCountry.uppercase()} REGIONAL MARKET TELEMETRY"
                    },
                    color = LightBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (viewModel.selectedCountry.equals("Worldwide", ignoreCase = true)) {
                    if (selectedRole != "All") "$selectedRole Jobs ${viewModel.currentFlag}" else "Live Job Market ${viewModel.currentFlag}"
                } else {
                    if (selectedRole != "All") "$selectedRole Jobs in ${viewModel.selectedCountry} ${viewModel.currentFlag}" else "Jobs in ${viewModel.selectedCountry} ${viewModel.currentFlag}"
                },
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Browse ${filteredJobs.size} active job listings across $uniqueCompanies companies.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // --- 2. Telemetry Stats Grid ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryStatCard(
                label = "ACTIVE SIGNALS",
                value = "$totalActiveJobs",
                subtext = "${paginatedJobs.size} on this page",
                valueColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            TelemetryStatCard(
                label = "HIRING ENTITIES",
                value = "$uniqueCompanies",
                subtext = "verified",
                valueColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            TelemetryStatCard(
                label = "MARKET SECTORS",
                value = "$uniqueRoles",
                subtext = "categories",
                valueColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            TelemetryStatCard(
                label = "SIGNAL INTEGRITY",
                value = "100%",
                subtext = "verified",
                valueColor = EmeraldGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // --- 3. Search & Filter Bar ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BgCard)
                .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        currentPage = 1
                    },
                    placeholder = {
                        Text(
                            text = "Search title or company...",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgDeep,
                        unfocusedContainerColor = BgDeep,
                        focusedBorderColor = LightBlue,
                        unfocusedBorderColor = BorderWhite10,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Role Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    allRoles.forEach { role ->
                        val isSelected = selectedRole.equals(role, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) PrimaryBlue else BgDeep
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) LightBlue else BorderWhite5,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedRole = role
                                    currentPage = 1
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = role.uppercase(),
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // --- 4. Stream Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = LightBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "STREAMING ${paginatedJobs.size} VERIFIED MARKET SIGNALS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (totalPages > 1) {
                Text(
                    text = "Page $currentPage of $totalPages",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // --- 5. Job Cards Stream or Empty State ---
        if (paginatedJobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(24.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "No Signals",
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "No Active Market Signals Found",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "No verified job listings matching your current filters.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgDeep)
                            .border(1.dp, BorderWhite10, RoundedCornerShape(12.dp))
                            .clickable {
                                searchQuery = ""
                                selectedRole = "All"
                                viewModel.selectCountry("Worldwide")
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "RESET FILTERS",
                            color = LightBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        } else {
            val context = LocalContext.current
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                paginatedJobs.forEachIndexed { index, job ->
                    val isSaved = viewModel.isJobSaved(job.id)
                    MarketJobCard(
                        job = job,
                        index = index,
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
        }

        // --- 6. Pagination Controls ---
        JobsPagination(
            currentPage = currentPage,
            totalPages = totalPages,
            totalJobs = totalActiveJobs,
            onPageChange = { currentPage = it }
        )

        // --- 7. Employer Post Job Callout Footer ---
        val context = LocalContext.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E1B4B)
                        )
                    )
                )
                .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Looking to hire top talent?",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Post job vacancies directly on JobsReport.online",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryBlue)
                        .clickable {
                            viewModel.openPostJob()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Post Job",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Post Job",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryStatCard(
    label: String,
    value: String,
    subtext: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .border(1.dp, BorderWhite10, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = valueColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = subtext,
                color = TextMuted,
                fontSize = 8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MarketJobCard(
    job: JobOpportunity,
    index: Int,
    isSaved: Boolean = false,
    onToggleSave: () -> Unit = {},
    onClick: () -> Unit = {}
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

            // Row 2: Role / Category Badge & Post time / Expiry
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        tint = TextMuted,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = job.postedAt ?: "Recent",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
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
