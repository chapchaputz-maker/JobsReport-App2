package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ads.NativeAdCard
import com.example.data.MarketReport
import com.example.state.AppViewModel
import com.example.state.NavRoute
import com.example.ui.components.ReportCard
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BorderWhite10
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPurple

@Composable
fun ReportsScreen(viewModel: AppViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedReportForDialog by remember { mutableStateOf<MarketReport?>(null) }

    val reports = viewModel.reports
    val selectedCountry = viewModel.selectedCountry
    val isWorldwide = selectedCountry.equals("Worldwide", ignoreCase = true)

    // Filter reports based on search query & selected country
    val filteredReports = remember(reports, searchQuery, selectedCountry, isWorldwide) {
        reports.filter { report ->
            val matchesSearch = searchQuery.isBlank() ||
                report.title.contains(searchQuery, ignoreCase = true) ||
                (report.role?.contains(searchQuery, ignoreCase = true) == true) ||
                (report.category?.contains(searchQuery, ignoreCase = true) == true) ||
                (report.summary?.contains(searchQuery, ignoreCase = true) == true) ||
                (report.excerpt?.contains(searchQuery, ignoreCase = true) == true)

            val matchesCountry = isWorldwide ||
                report.country?.contains(selectedCountry, ignoreCase = true) == true

            matchesSearch && matchesCountry
        }
    }

    val countriesWithReports = remember(reports) {
        reports.mapNotNull { it.country }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val recentReports = remember(filteredReports) {
        filteredReports.take(5)
    }

    val distinctIndustries = remember(reports) {
        reports.mapNotNull { it.role ?: it.category }.filter { it.isNotBlank() }.distinct().size
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. Breadcrumb Navigation ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "HOME",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.clickable { viewModel.navigateTo(NavRoute.INTELLIGENCE_FEED) }
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(12.dp)
            )
            if (!isWorldwide) {
                Text(
                    text = "REPORTS",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable { viewModel.selectCountry("Worldwide") }
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = selectedCountry.uppercase(),
                    color = LightBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Text(
                    text = "REPORTS",
                    color = LightBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // --- 2. Page Header ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = LightBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MARKET INTELLIGENCE ARCHIVES",
                    color = LightBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = if (isWorldwide) {
                    "Job Market Reports & Hiring Trend Analysis"
                } else {
                    "Job Market Reports in $selectedCountry"
                },
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )

            Text(
                text = if (isWorldwide) {
                    "Aggregated regional analysis reports mapping market growth telemetry, demand spikes, and active employer placements. Sorted chronologically."
                } else {
                    "Employment reports, hiring trends, and labor market insights for $selectedCountry. Analysis of industry demand, salary benchmarks, and career opportunities."
                },
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // Key Stats Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatPill(
                    icon = Icons.Default.Book,
                    iconColor = LightBlue,
                    value = "${reports.size}",
                    label = "Reports"
                )
                StatPill(
                    icon = Icons.Default.Language,
                    iconColor = EmeraldGreen,
                    value = "${countriesWithReports.size}",
                    label = "Countries"
                )
                StatPill(
                    icon = Icons.Default.TrendingUp,
                    iconColor = VioletPurple,
                    value = "$distinctIndustries",
                    label = "Industries"
                )
            }
        }

        // --- 3. Filter & Search Bar ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BgCard)
                .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search report titles, roles, industries...",
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
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LightBlue.copy(alpha = 0.5f),
                    unfocusedBorderColor = BorderWhite10,
                    focusedContainerColor = BgDeep,
                    unfocusedContainerColor = BgDeep,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "COUNTRY:",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgDeep)
                            .border(1.dp, BorderWhite10, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${viewModel.currentFlag} $selectedCountry",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (searchQuery.isNotEmpty() || !isWorldwide) {
                    Text(
                        text = "RESET FILTERS",
                        color = LightBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable {
                            searchQuery = ""
                            viewModel.selectCountry("Worldwide")
                        }
                    )
                }
            }
        }

        // --- 4. Main Section: Reports List ---
        if (filteredReports.isEmpty()) {
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "No Reports",
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "No Matching Reports Found",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "Try adjusting your search terms or clearing filters."
                        } else {
                            "No reports available for $selectedCountry yet. Check back soon or browse worldwide reports."
                        },
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = {
                            searchQuery = ""
                            viewModel.selectCountry("Worldwide")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LightBlue.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "CLEAR FILTERS",
                            color = LightBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredReports.forEachIndexed { index, report ->
                    ReportCard(
                        report = report,
                        onClick = { viewModel.selectReport(report) }
                    )
                    if ((index + 1) % 3 == 0) {
                        NativeAdCard()
                    }
                }
            }
        }

        // --- 5. Sidebar & Supplementary Section ---
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Reports by Country
            if (countriesWithReports.isNotEmpty()) {
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
                            text = "REPORTS BY COUNTRY",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )

                        countriesWithReports.take(8).forEach { country ->
                            val count = reports.count { it.country.equals(country, ignoreCase = true) }
                            val isActive = selectedCountry.equals(country, ignoreCase = true)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isActive) LightBlue.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { viewModel.selectCountry(country) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = country,
                                    color = if (isActive) LightBlue else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "$count",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Market Telemetry Highlights
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "MARKET HIGHLIGHTS",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )

                    HighlightRow(
                        badge = "+45%",
                        badgeColor = EmeraldGreen,
                        title = "AI Specialist Surge",
                        desc = "High demand across deep neural processing and LLM deployment operations."
                    )

                    HighlightRow(
                        badge = "+28%",
                        badgeColor = LightBlue,
                        title = "Dar es Salaam Tech Hub",
                        desc = "Tanzanian fintech expansion and digital remittance portals driving placements."
                    )

                    HighlightRow(
                        badge = "+22%",
                        badgeColor = VioletPurple,
                        title = "Software Developer Stacks",
                        desc = "Increased demand for Senior React frameworks and backend service engineering."
                    )
                }
            }

            // Data Sources Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(LightBlue.copy(alpha = 0.08f))
                    .border(1.dp, LightBlue.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Data Sources",
                        tint = LightBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "DATA SOURCES & TELEMETRY",
                            color = LightBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "JobsReport publishes market intelligence reports based on verified job postings, corporate placements, and regional labor metrics.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }

    // --- Report Detail Modal Dialog ---
    selectedReportForDialog?.let { report ->
        Dialog(onDismissRequest = { selectedReportForDialog = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BgCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderWhite10, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VioletPurple.copy(alpha = 0.2f))
                                .border(1.dp, VioletPurple.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = report.displayCategory.uppercase(),
                                color = VioletPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = { selectedReportForDialog = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Close",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = report.title,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "BY ${report.author ?: "JobsReport Research"}",
                            color = LightBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "•",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                        Text(
                            text = report.displayDate,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (!report.country.isNullOrBlank()) {
                            Text(
                                text = "•",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = report.country,
                                color = EmeraldGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgDeep)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = report.displaySummary.ifBlank {
                                "Comprehensive labor market report detailing industry trends, hiring demands, salary benchmarks, and growth corridors across regional markets."
                            },
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    Button(
                        onClick = { selectedReportForDialog = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LightBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "CLOSE REPORT",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    value: String,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun HighlightRow(
    badge: String,
    badgeColor: Color,
    title: String,
    desc: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = badge,
                color = badgeColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = desc,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
