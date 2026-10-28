package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import coil.compose.AsyncImage
import com.example.ads.AdConfig
import com.example.ads.NativeAdCard
import com.example.data.JobCategory
import com.example.data.JobOpportunity
import com.example.data.MarketReport
import com.example.data.isJobExpired
import com.example.data.resolvePosterName
import com.example.data.resolvePosterAvatar
import com.example.data.resolvePosterUserId
import com.example.state.AppViewModel
import com.example.state.NavRoute
import com.example.ui.components.ReportCard
import com.example.ui.components.getRoleIcon
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IntelligenceFeedScreen(viewModel: AppViewModel) {
    var showAllCategories by remember { mutableStateOf(false) }

    val isTanzania = viewModel.selectedCountry.equals("Tanzania", ignoreCase = true)

    // Filter active jobs based on selected country
    val countryJobs = remember(viewModel.activeJobs, viewModel.selectedCountry) {
        viewModel.activeJobs.filter { job ->
            viewModel.selectedCountry.equals("Worldwide", ignoreCase = true) ||
            viewModel.selectedCountry.isBlank() ||
            (job.country?.contains(viewModel.selectedCountry, ignoreCase = true) == true) ||
            (job.location?.contains(viewModel.selectedCountry, ignoreCase = true) == true) ||
            (job.company.contains(viewModel.selectedCountry, ignoreCase = true))
        }
    }
    val homeJobs = countryJobs.take(6)

    val displayedCategories = if (showAllCategories) {
        viewModel.categories
    } else {
        viewModel.categories.take(10)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. Live Radar Header Badge ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BgCard)
                .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REAL-TIME TALENT INTELLIGENCE",
                    color = LightBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${viewModel.currentFlag} ${viewModel.selectedCountry}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                if (viewModel.isDataLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = LightBlue,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = { viewModel.fetchDashboardData() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Data",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // --- Community Stories (flat Facebook-style rail, no card) ---
        Column(modifier = Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen)
                    )
                    Text(
                        text = "STORIES",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Create Story — plain text, no pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.isCreateStoryOpen = true
                            }
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Story",
                            tint = LightBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Create Story",
                            color = LightBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // View All
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.navigateTo(NavRoute.STORIES) }
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "View All",
                            color = LightBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = LightBlue,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            com.example.ui.screens.StoryReel(
                stories = viewModel.stories,
                loading = viewModel.isStoriesLoading,
                currentUser = viewModel.currentUser,
                isUserVerified = { uid, u -> viewModel.isUserVerified(uid, u) },
                authorProvider = { uid, u -> viewModel.getResolvedUser(uid, u) },
                onCreateStory = {
                    viewModel.isCreateStoryOpen = true
                },
                onViewStory = { story ->
                    viewModel.viewingStory = story
                },
                onProfileClick = { authorId ->
                    val fallback = viewModel.stories.firstOrNull { it.userId == authorId }?.user
                    val storyAuthor = viewModel.getResolvedUser(authorId, fallback)
                    viewModel.openUserProfile(storyAuthor)
                },
                onRequestLogin = {
                    viewModel.openAuth()
                },
                flat = true,
                onLoadMore = {
                    viewModel.loadStories()
                },
                rotationSeed = viewModel.storyRotationSeed
            )
        }

        // --- 2. Hero Section ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            BgCard
                        )
                    )
                )
                .border(1.dp, BorderWhite10, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = "Radar",
                        tint = LightBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTanzania) "TANZANIA OPPORTUNITY RADAR" else "GLOBAL CAREER FEED",
                        color = LightBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isTanzania) {
                        "Jobs in Tanzania 🇹🇿 Latest Vacancies."
                    } else {
                        "Find Your Next Career Opportunity."
                    },
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Live market telemetry, verified salary benchmarks, and direct employer talent pipelines.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Stat Pills Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatPill(
                        label = "${viewModel.categories.size} Categories",
                        icon = Icons.Default.Work,
                        color = LightBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatPill(
                        label = "${viewModel.reports.size} Reports",
                        icon = Icons.Default.MenuBook,
                        color = VioletPurple,
                        modifier = Modifier.weight(1f)
                    )
                    StatPill(
                        label = "${countryJobs.size} Jobs",
                        icon = Icons.Default.TrendingUp,
                        color = EmeraldGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- Native Ad 1: Top Feed Placement (Unit 1) ---
        NativeAdCard(adUnitId = AdConfig.getNativeAdUnitId(0))

        // --- 3. Job Categories Grid Section ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Job Categories",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (showAllCategories) "Showing all ${viewModel.categories.size} categories" else "Top 10 of ${viewModel.categories.size} categories",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgCard)
                        .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                        .clickable { showAllCategories = !showAllCategories }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showAllCategories) "Show Less" else "See More",
                        color = LightBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (showAllCategories) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Categories",
                        tint = LightBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Category Cards 2-column Flow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                displayedCategories.forEach { category ->
                    CategoryCard(
                        category = category,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectCategory(category.name) }
                    )
                }
            }
        }

        // --- Native Ad 2: After Categories Placement (Unit 2) ---
        NativeAdCard(adUnitId = AdConfig.getNativeAdUnitId(1))

        // --- 4. Latest Opportunities (Active Jobs) ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Sparkles",
                        tint = AmberYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Latest Opportunities",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (viewModel.selectedCountry.equals("Worldwide", ignoreCase = true)) {
                                "Showing 6 of ${countryJobs.size} active jobs worldwide"
                            } else {
                                "Showing 6 of ${countryJobs.size} active jobs in ${viewModel.selectedCountry}"
                            },
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(NavRoute.ALL_JOBS) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View All",
                        color = LightBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View All Jobs",
                        tint = LightBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (homeJobs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BgCard)
                        .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No active jobs listed for ${viewModel.selectedCountry}",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try switching to Worldwide or select another country.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryBlue)
                                .clickable { viewModel.selectCountry("Worldwide") }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "View Worldwide Jobs",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                val context = LocalContext.current
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    homeJobs.forEach { job ->
                        val isSaved = viewModel.isJobSaved(job.id)
                        JobCardItem(
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
                            onPosterClick = { userId, name, avatar ->
                                viewModel.openUserProfileByName(userId, name, avatar)
                            },
                            onClick = { viewModel.selectJob(job) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // See More button opening Joblistscreen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(PrimaryBlue, VioletPurple)
                                )
                            )
                            .clickable { viewModel.navigateTo(NavRoute.ALL_JOBS) }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (countryJobs.size > 6) "SEE MORE JOBS (${countryJobs.size - 6} MORE)" else "SEE ALL JOBS",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "See More Jobs",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Native Ad 3: Middle Feed Placement (Unit 3) ---
        NativeAdCard(adUnitId = AdConfig.getNativeAdUnitId(2))

        // --- 5. Market Intelligence Reports ---
        if (viewModel.reports.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Market Intelligence Reports",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Salary benchmarks & industry talent analysis",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(NavRoute.JOB_REPORTS) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "All Reports",
                            color = VioletPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "All Reports",
                            tint = VioletPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    viewModel.reports.forEach { report ->
                        ReportCard(
                            report = report,
                            onClick = { viewModel.selectReport(report) }
                        )
                    }
                }
            }
        }

        // --- 6. Explore Jobs by Country ---
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
                        imageVector = Icons.Default.Public,
                        contentDescription = "Globe",
                        tint = LightBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Explore Jobs by Country",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.countries.forEach { c ->
                        val isSelected = viewModel.selectedCountry.equals(c.name, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PrimaryBlue.copy(alpha = 0.2f) else BgDeep)
                                .border(
                                    1.dp,
                                    if (isSelected) LightBlue else BorderWhite5,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectCountry(c.name) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${c.flag} ${c.name}",
                                color = if (isSelected) LightBlue else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgDeep)
                        .border(1.dp, BorderWhite10, RoundedCornerShape(12.dp))
                        .clickable { viewModel.navigateTo(NavRoute.REGIONS) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Browse Jobs by City & Region",
                            color = LightBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Go to Regions",
                            tint = LightBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // --- Native Ad 4: Pre-Spotlight Placement (Unit 4) ---
        NativeAdCard(adUnitId = AdConfig.getNativeAdUnitId(3))

        // --- 7. Weekly Spotlight Employers ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1E1B4B),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(1.dp, VioletPurple.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Spotlight",
                        tint = AmberYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WEEKLY EMPLOYER SPOTLIGHT",
                        color = AmberYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Featured organizations actively recruiting verified talent this week:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.spotlightCompanies.forEach { comp ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(BgCard)
                                .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                                .clickable { viewModel.navigateTo(NavRoute.COMPANIES) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = comp,
                                    tint = LightBlue,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = comp,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Native Ad 5: Bottom Feed Placement (Unit 5) ---
        NativeAdCard(adUnitId = AdConfig.getNativeAdUnitId(4))

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun StatPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BgDeep)
            .border(1.dp, BorderWhite5, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: JobCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val categoryIcon = getRoleIcon(category.name, category.slug)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = category.name,
                    tint = LightBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = category.name,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${category.totalCount} active",
                color = EmeraldGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun JobCardItem(
    job: JobOpportunity,
    isSaved: Boolean = false,
    onToggleSave: () -> Unit = {},
    onPosterClick: (userId: String, name: String, avatarUrl: String?) -> Unit = { _, _, _ -> },
    onClick: () -> Unit
) {
    val isExpired = job.isJobExpired()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isExpired) BgCard.copy(alpha = 0.6f) else BgCard)
            .border(1.dp, if (isExpired) BorderWhite5 else BorderWhite10, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Row 1: Logo + (Title & Company) + Bookmark Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Logo or Placeholder Initials
                if (!job.logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = job.logoUrl,
                        contentDescription = job.company,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BgDeep)
                            .border(1.dp, BorderWhite10, RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryBlue.copy(alpha = 0.2f))
                            .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = job.company.take(1).uppercase(),
                            color = LightBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Title and Company
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = job.title,
                        color = if (isExpired) TextSecondary else TextPrimary,
                        fontSize = 14.sp,
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
                            .background(if (isExpired) DangerRed.copy(alpha = 0.15f) else BgDeep)
                            .border(1.dp, if (isExpired) DangerRed.copy(alpha = 0.3f) else BorderWhite5, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isExpired) "EXPIRED" else job.role,
                            color = if (isExpired) DangerRed else LightBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
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
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time",
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

            // Row 3: Location and Salary Footer
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
                }
            }

            // Row 4: Posted By User (Click opens user profile) - only if poster exists
            val posterName = job.resolvePosterName()
            if (!posterName.isNullOrBlank()) {
                val nonNullName = posterName
                val posterAvatar = job.resolvePosterAvatar()
                val nonNullUserId = job.resolvePosterUserId()?.takeIf { it.isNotBlank() } ?: nonNullName

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderWhite10.copy(alpha = 0.5f))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onPosterClick(nonNullUserId, nonNullName, posterAvatar)
                        }
                        .padding(vertical = 2.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!posterAvatar.isNullOrBlank()) {
                        AsyncImage(
                            model = posterAvatar,
                            contentDescription = posterName,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(BgDeep)
                                .border(1.dp, PrimaryBlue.copy(alpha = 0.4f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "By",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = posterName,
                            color = LightBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
