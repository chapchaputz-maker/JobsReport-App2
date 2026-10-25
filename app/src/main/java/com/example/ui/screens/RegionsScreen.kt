package com.example.ui.screens

import com.example.ads.NativeAdCard
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocationInfo
import com.example.state.AppViewModel
import com.example.state.NavRoute
import com.example.ui.theme.AmberYellow
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BorderWhite10
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPurple

data class RegionDataModel(
    val name: String,
    val country: String,
    val totalJobs: Int,
    val activeJobs: Int,
    val rawLocationInfo: LocationInfo
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegionsScreen(viewModel: AppViewModel) {
    LaunchedEffect(Unit) {
        viewModel.fetchLocations()
    }

    val selectedCountry = viewModel.selectedCountry
    val isWorldwide = selectedCountry.equals("Worldwide", ignoreCase = true)
    var searchTerm by remember { mutableStateOf("") }

    // Filter locations by country if not worldwide
    val baseLocations = if (isWorldwide) {
        viewModel.locations
    } else {
        viewModel.locations.filter { loc ->
            loc.country?.equals(selectedCountry, ignoreCase = true) == true
        }
    }

    // Deduplicate regions by unique region name lowercased
    val uniqueLocationsMap = remember(baseLocations) {
        val map = mutableMapOf<String, LocationInfo>()
        baseLocations.forEach { loc ->
            val key = loc.name.lowercase().trim()
            if (!map.containsKey(key)) {
                map[key] = loc
            }
        }
        map.values.toList()
    }

    // Calculate jobs per region
    val allRegionData = remember(uniqueLocationsMap, viewModel.activeJobs) {
        uniqueLocationsMap.map { loc ->
            val regionNameLower = loc.name.lowercase()
            val matchingJobs = viewModel.activeJobs.filter { job ->
                val jobLoc = (job.location ?: "").lowercase()
                val jobCity = (job.city ?: "").lowercase()
                val jobReg = (job.region ?: "").lowercase()
                jobLoc.contains(regionNameLower) || jobCity.contains(regionNameLower) || jobReg.contains(regionNameLower)
            }
            val activeCount = matchingJobs.count { it.active != false }
            RegionDataModel(
                name = loc.name,
                country = loc.country ?: "Worldwide",
                totalJobs = matchingJobs.size.coerceAtLeast(loc.activeJobsCount ?: 0),
                activeJobs = if (activeCount > 0) activeCount else (loc.activeJobsCount ?: 0),
                rawLocationInfo = loc
            )
        }.sortedWith(
            compareByDescending<RegionDataModel> { it.activeJobs > 0 }
                .thenByDescending { it.activeJobs }
                .thenBy { it.name }
        )
    }

    // Filter by search query
    val filteredRegions = remember(allRegionData, searchTerm) {
        if (searchTerm.isBlank()) {
            allRegionData
        } else {
            val query = searchTerm.trim().lowercase()
            allRegionData.filter {
                it.name.lowercase().contains(query) || it.country.lowercase().contains(query)
            }
        }
    }

    val activeRegions = filteredRegions.filter { it.activeJobs > 0 }
    val emptyRegions = filteredRegions.filter { it.activeJobs == 0 }

    val totalActiveJobs = activeRegions.sumOf { it.activeJobs }
    val totalLocationsCount = filteredRegions.size
    val activeLocationsCount = activeRegions.size

    // Grouping by country for Worldwide view
    val groupedByCountry = remember(activeRegions, isWorldwide) {
        if (isWorldwide) {
            activeRegions.groupBy { it.country }
        } else {
            emptyMap()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Header Banner ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Regions",
                                tint = AmberYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REGIONAL JOB EXPLORER",
                                color = AmberYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isWorldwide) "Jobs by City & Region" else "Jobs by Region in $selectedCountry",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isWorldwide)
                            "Browse job opportunities across $activeLocationsCount cities and regions worldwide. $totalActiveJobs active jobs available."
                        else
                            "Browse job opportunities across $activeLocationsCount regions in $selectedCountry. $totalActiveJobs active jobs available.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Badges Row
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = AmberYellow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$activeLocationsCount Active Regions",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalActiveJobs Active Jobs",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (totalLocationsCount > activeLocationsCount) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$totalLocationsCount Total Locations",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Search Input ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchTerm.isEmpty()) {
                            Text(
                                text = if (isWorldwide) "Search cities or countries..." else "Search regions in $selectedCountry...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                        BasicTextField(
                            value = searchTerm,
                            onValueChange = { searchTerm = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(AmberYellow),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchTerm.isNotEmpty()) {
                        IconButton(
                            onClick = { searchTerm = "" },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. Empty Search / No Regions State ---
        if (filteredRegions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgCard)
                        .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Regions Found",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchTerm.isNotEmpty()) "No regions match your search '$searchTerm'." else "No locations available.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // --- 4. Worldwide View (Grouped by Country) ---
        if (isWorldwide && groupedByCountry.isNotEmpty()) {
            groupedByCountry.forEach { (countryName, countryRegions) ->
                item(key = "country_header_$countryName") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = LightBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = countryName.uppercase(),
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${countryRegions.size} region${if (countryRegions.size > 1) "s" else ""}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "VIEW COUNTRY →",
                            color = PrimaryBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.clickable {
                                viewModel.selectCountry(countryName)
                            }
                        )
                    }
                }

                items(countryRegions, key = { "region_${it.name}_$countryName" }) { region ->
                    RegionCardItem(
                        region = region,
                        onClick = { viewModel.selectRegion(region.name, region.rawLocationInfo) }
                    )
                }
            }
        }

        // --- 5. Specific Country Active Regions Grid/List ---
        if (!isWorldwide && activeRegions.isNotEmpty()) {
            item {
                NativeAdCard()
            }
            items(activeRegions, key = { "region_${it.name}" }) { region ->
                RegionCardItem(
                    region = region,
                    onClick = { viewModel.selectRegion(region.name, region.rawLocationInfo) }
                )
            }
        }

        // --- 6. Other Locations Section (0 active jobs) ---
        if (emptyRegions.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "OTHER LOCATIONS (${emptyRegions.size})",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No active jobs yet",
                            color = TextMuted.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        emptyRegions.take(20).forEach { reg ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(BgCard.copy(alpha = 0.5f))
                                    .border(1.dp, BorderWhite10.copy(alpha = 0.5f), CircleShape)
                                    .clickable { viewModel.selectRegion(reg.name, reg.rawLocationInfo) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = reg.name,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        if (emptyRegions.size > 20) {
                            Text(
                                text = "+${emptyRegions.size - 20} more",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }

        // --- 7. No Active Jobs in Country Fallback ---
        if (!isWorldwide && activeRegions.isEmpty() && filteredRegions.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgCard)
                        .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Active Jobs in $selectedCountry Regions",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "We have ${filteredRegions.size} location${if (filteredRegions.size > 1) "s" else ""} listed for $selectedCountry, but no active jobs currently.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.navigateTo(NavRoute.ALL_JOBS) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "BROWSE ALL JOBS IN $selectedCountry",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegionCardItem(
    region: RegionDataModel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
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
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmberYellow.copy(alpha = 0.12f))
                        .border(1.dp, AmberYellow.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AmberYellow,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = region.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = region.country,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(VioletPurple.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${region.activeJobs} ACTIVE",
                            color = LightBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (region.totalJobs > region.activeJobs) {
                        Text(
                            text = "(${region.totalJobs} total)",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View Region",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
