package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ads.NativeAdCard
import com.example.data.Company
import com.example.data.JobOpportunity
import com.example.state.AppViewModel
import com.example.ui.theme.AmberYellow
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BorderWhite10
import com.example.ui.theme.BorderWhite5
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPurple
import kotlin.math.ceil

private const val COMPANIES_PER_PAGE = 12

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompaniesScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val selectedCompany = viewModel.selectedCompany
    var searchTerm by remember { mutableStateOf("") }
    var currentPage by remember { mutableIntStateOf(1) }

    val filteredCompanies = remember(viewModel.companies, searchTerm) {
        if (searchTerm.isBlank()) {
            viewModel.companies
        } else {
            viewModel.companies.filter { it.name.contains(searchTerm, ignoreCase = true) }
        }
    }

    val totalPages = remember(filteredCompanies.size) {
        val pages = ceil(filteredCompanies.size.toDouble() / COMPANIES_PER_PAGE).toInt()
        if (pages < 1) 1 else pages
    }

    val paginatedCompanies = remember(filteredCompanies, currentPage) {
        val startIndex = (currentPage - 1) * COMPANIES_PER_PAGE
        if (startIndex >= filteredCompanies.size) {
            emptyList()
        } else {
            filteredCompanies.subList(
                startIndex,
                minOf(startIndex + COMPANIES_PER_PAGE, filteredCompanies.size)
            )
        }
    }

    val totalActiveJobs = remember(viewModel.companies) {
        viewModel.companies.sumOf { it.activeJobs ?: 0 }
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(selectedCompany?.id, currentPage) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. Page Title Header ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = "Employer Directory",
                    tint = LightBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "EMPLOYER DIRECTORY",
                    color = LightBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = selectedCompany?.name ?: "Companies & Employers",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = if (selectedCompany != null) {
                    "Browse jobs and corporate information for ${selectedCompany.name}."
                } else {
                    "Browse top companies and verified corporate recruiters actively hiring."
                },
                color = TextSecondary,
                fontSize = 13.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Companies",
                        tint = LightBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${if (selectedCompany != null) 1 else viewModel.companies.size} Companies",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = "Active Jobs",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$totalActiveJobs Active Jobs",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // --- 2. Live Search Box (When Directory View) ---
        if (selectedCompany == null) {
            OutlinedTextField(
                value = searchTerm,
                onValueChange = {
                    searchTerm = it
                    currentPage = 1
                },
                placeholder = {
                    Text(
                        text = "Search ${viewModel.companies.size} companies...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgCard),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LightBlue.copy(alpha = 0.5f),
                    unfocusedBorderColor = BorderWhite10,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )
        }

        // --- 3. Selected Company Detail View ---
        if (selectedCompany != null) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Back Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.selectCompany(null) }
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = LightBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BACK TO ALL COMPANIES",
                        color = LightBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                // Header Overview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgCard)
                        .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (!selectedCompany.logoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = selectedCompany.logoUrl,
                                    contentDescription = selectedCompany.name,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(BgDeep)
                                        .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(PrimaryBlue, VioletPurple)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedCompany.name.take(1).uppercase(),
                                        color = Color.White,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedCompany.name,
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (!selectedCompany.industry.isNullOrBlank()) {
                                    Text(
                                        text = selectedCompany.industry,
                                        color = VioletPurple,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Badges Row
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BgDeep)
                                    .border(1.dp, BorderWhite5, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${selectedCompany.activeJobs ?: 0} Active Jobs",
                                    color = EmeraldGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BgDeep)
                                    .border(1.dp, BorderWhite5, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${selectedCompany.totalJobs ?: 0} Total Listings",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            if (!selectedCompany.foundedYear.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BgDeep)
                                        .border(1.dp, BorderWhite5, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Founded",
                                        tint = TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Founded ${selectedCompany.foundedYear}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (!selectedCompany.employeeCount.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BgDeep)
                                        .border(1.dp, BorderWhite5, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = "Employees",
                                        tint = TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${selectedCompany.employeeCount} employees",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Details Grid (Location & Business Info)
                val hasLocationInfo = !selectedCompany.streetAddress.isNullOrBlank() ||
                        !selectedCompany.area.isNullOrBlank() ||
                        !selectedCompany.locality.isNullOrBlank() ||
                        !selectedCompany.district.isNullOrBlank() ||
                        !selectedCompany.postalCode.isNullOrBlank() ||
                        !selectedCompany.country.isNullOrBlank()

                val hasBusinessInfo = !selectedCompany.url.isNullOrBlank() ||
                        !selectedCompany.industry.isNullOrBlank() ||
                        !selectedCompany.foundedYear.isNullOrBlank() ||
                        !selectedCompany.employeeCount.isNullOrBlank()

                if (hasLocationInfo) {
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
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LOCATION",
                                    color = EmeraldGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }

                            if (!selectedCompany.streetAddress.isNullOrBlank()) {
                                MetaRow(label = "STREET", value = selectedCompany.streetAddress)
                            }
                            if (!selectedCompany.area.isNullOrBlank()) {
                                MetaRow(label = "AREA", value = selectedCompany.area)
                            }
                            if (!selectedCompany.locality.isNullOrBlank()) {
                                MetaRow(label = "CITY", value = selectedCompany.locality)
                            }
                            if (!selectedCompany.district.isNullOrBlank()) {
                                MetaRow(label = "DISTRICT", value = selectedCompany.district)
                            }
                            if (!selectedCompany.postalCode.isNullOrBlank()) {
                                MetaRow(
                                    label = "POSTAL",
                                    value = "${selectedCompany.postalCode}${if (!selectedCompany.postalArea.isNullOrBlank()) " (${selectedCompany.postalArea})" else ""}"
                                )
                            }
                            if (!selectedCompany.country.isNullOrBlank()) {
                                MetaRow(
                                    label = "COUNTRY",
                                    value = if (selectedCompany.country == "TZ") "🇹🇿 Tanzania" else selectedCompany.country,
                                    isHighlight = true
                                )
                            }
                        }
                    }
                }

                if (hasBusinessInfo) {
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
                                    imageVector = Icons.Default.Work,
                                    contentDescription = "Business Info",
                                    tint = LightBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "BUSINESS INFO",
                                    color = LightBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }

                            if (!selectedCompany.url.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "WEBSITE",
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(selectedCompany.url))
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
                                            text = selectedCompany.url.removePrefix("https://").removePrefix("http://").removePrefix("www."),
                                            color = LightBlue,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Launch,
                                            contentDescription = "Open",
                                            tint = LightBlue,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }

                            if (!selectedCompany.industry.isNullOrBlank()) {
                                MetaRow(label = "INDUSTRY", value = selectedCompany.industry)
                            }
                            if (!selectedCompany.foundedYear.isNullOrBlank()) {
                                MetaRow(label = "FOUNDED", value = selectedCompany.foundedYear)
                            }
                            if (!selectedCompany.employeeCount.isNullOrBlank()) {
                                MetaRow(label = "EMPLOYEES", value = selectedCompany.employeeCount)
                            }
                        }
                    }
                }

                // Top Native Ad for Company Detail View
                NativeAdCard()

                // About Description Section
                if (!selectedCompany.description.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(BgCard)
                            .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "ABOUT ${selectedCompany.name.uppercase()}",
                                color = AmberYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = selectedCompany.description,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Company Jobs Openings List
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "JOB OPENINGS (${viewModel.selectedCompanyJobs.size})",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    if (viewModel.isCompanyJobsLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = LightBlue)
                        }
                    } else if (viewModel.selectedCompanyJobs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(BgCard)
                                .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No job listings currently available for this company.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            viewModel.selectedCompanyJobs.forEachIndexed { index, job ->
                                CompanyJobCard(
                                    job = job,
                                    onClick = { viewModel.selectJob(job) }
                                )
                                if ((index + 1) % 3 == 0) {
                                    NativeAdCard()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // --- 4. Directory Grid View of Companies ---
            if (paginatedCompanies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No companies found matching '$searchTerm'.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    paginatedCompanies.forEachIndexed { index, company ->
                        CompanyDirectoryCard(
                            company = company,
                            onClick = { viewModel.selectCompany(company) }
                        )
                        if ((index + 1) % 3 == 0) {
                            NativeAdCard()
                        }
                    }
                }

                // Pagination Controls
                if (totalPages > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (currentPage > 1) BgCard else BgDeep)
                                .border(1.dp, BorderWhite10, RoundedCornerShape(12.dp))
                                .clickable(enabled = currentPage > 1) { currentPage-- }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Prev",
                                    tint = if (currentPage > 1) TextPrimary else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PREV",
                                    color = if (currentPage > 1) TextPrimary else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Text(
                            text = "PAGE $currentPage OF $totalPages",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (currentPage < totalPages) BgCard else BgDeep)
                                .border(1.dp, BorderWhite10, RoundedCornerShape(12.dp))
                                .clickable(enabled = currentPage < totalPages) { currentPage++ }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "NEXT",
                                    color = if (currentPage < totalPages) TextPrimary else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next",
                                    tint = if (currentPage < totalPages) TextPrimary else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanyDirectoryCard(
    company: Company,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!company.logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = company.logoUrl,
                        contentDescription = company.name,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BgDeep)
                            .border(1.dp, BorderWhite10, RoundedCornerShape(14.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryBlue, VioletPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = company.name.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = company.name,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!company.industry.isNullOrBlank()) {
                        Text(
                            text = company.industry,
                            color = VioletPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = "Jobs",
                        tint = LightBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${company.activeJobs ?: 0} active jobs",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if ((company.totalJobs ?: 0) > (company.activeJobs ?: 0)) {
                        Text(
                            text = " (${company.totalJobs} total)",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CompanyJobCard(
    job: JobOpportunity,
    onClick: () -> Unit
) {
    val isActive = job.active != false
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isActive) PrimaryBlue.copy(alpha = 0.15f) else AmberYellow.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isActive) "ACTIVE" else "EXPIRED",
                        color = if (isActive) LightBlue else AmberYellow,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = job.role ?: "General",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = job.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = job.location ?: "Remote",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                if (!job.salary.isNullOrBlank()) {
                    Text(
                        text = job.salary,
                        color = EmeraldGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = if (isHighlight) EmeraldGreen else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
        )
    }
}
