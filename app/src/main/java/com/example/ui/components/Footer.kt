package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Country
import com.example.state.AppViewModel
import com.example.state.NavRoute
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BgSurface
import com.example.ui.theme.BorderWhite10
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TechnicalFooter(viewModel: AppViewModel? = null) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSurface)
            .border(width = 1.dp, color = BorderWhite10)
    ) {
        // --- Expanded Footer Sections ---
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgDeep)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Branding Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "JOBSREPORT.ONLINE",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Global Job Market Telemetry & Career Intelligence",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldGreen.copy(alpha = 0.15f))
                            .border(1.dp, EmeraldGreen.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTIVE",
                            color = EmeraldGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                HorizontalDivider(color = BorderWhite10)

                // ALL COUNTRIES SECTION
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Countries",
                            tint = LightBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ALL COUNTRIES & REGIONAL MARKETS",
                            color = LightBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    val allCountries = listOf(Country("WW", "Worldwide", "🌍")) + (viewModel?.countries ?: emptyList())

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(allCountries) { country ->
                            val isSelected = viewModel?.selectedCountry?.equals(country.name, ignoreCase = true) == true
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryBlue else BgCard)
                                    .border(
                                        1.dp,
                                        if (isSelected) LightBlue else BorderWhite10,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        viewModel?.selectCountry(country.name)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = country.flag, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = country.name.uppercase(),
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = BorderWhite10)

                // 3 COLUMNS: QUICK LINKS, LEGAL, CONNECT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Quick Links
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "QUICK LINKS",
                            color = LightBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        FooterLinkItem(label = "Live Market") {
                            viewModel?.navigateTo(NavRoute.INTELLIGENCE_FEED)
                        }
                        FooterLinkItem(label = "All Vacancies") {
                            viewModel?.navigateTo(NavRoute.ALL_JOBS)
                        }
                        FooterLinkItem(label = "Hiring Companies") {
                            viewModel?.navigateTo(NavRoute.COMPANIES)
                        }
                        FooterLinkItem(label = "Job Regions") {
                            viewModel?.navigateTo(NavRoute.REGIONS)
                        }
                        FooterLinkItem(label = "Job Stories") {
                            viewModel?.navigateTo(NavRoute.STORIES)
                        }
                        FooterLinkItem(label = "Market Reports") {
                            viewModel?.navigateTo(NavRoute.JOB_REPORTS)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Legal
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "LEGAL",
                            color = LightBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        FooterLinkItem(label = "About Us") {
                            viewModel?.navigateTo(NavRoute.ABOUT_US)
                        }
                        FooterLinkItem(label = "Privacy Policy") {
                            viewModel?.navigateTo(NavRoute.PRIVACY_POLICY)
                        }
                        FooterLinkItem(label = "Disclaimer") {
                            viewModel?.navigateTo(NavRoute.DISCLAIMER)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Connect & More
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "CONNECT",
                            color = LightBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        FooterLinkItem(label = "Contact Support") {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:jjovinatha@gmail.com")
                            }
                            context.startActivity(Intent.createChooser(intent, "Contact Support"))
                        }
                        FooterLinkItem(label = "Post Job") {
                            viewModel?.openPostJob()
                        }
                        FooterLinkItem(label = "v2.4.0 Active") {}
                    }
                }
            }
        }

        // --- Bottom Bar Toggle & Copyright ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Navigation Links & Footer Expand Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ABOUT",
                    color = PrimaryBlue,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel?.navigateTo(NavRoute.ABOUT_US)
                    }
                )

                Text(text = "•", color = TextMuted, fontSize = 10.sp)

                Text(
                    text = "DISCLAIMER",
                    color = PrimaryBlue,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel?.navigateTo(NavRoute.DISCLAIMER)
                    }
                )

                Text(text = "•", color = TextMuted, fontSize = 10.sp)

                Text(
                    text = "PRIVACY",
                    color = PrimaryBlue,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel?.navigateTo(NavRoute.PRIVACY_POLICY)
                    }
                )

                Text(text = "•", color = TextMuted, fontSize = 10.sp)

                // Expand / Collapse Footer Toggle Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BgCard)
                        .border(1.dp, BorderWhite10, RoundedCornerShape(6.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isExpanded) "LESS" else "MORE / COUNTRIES",
                        color = LightBlue,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Footer",
                        tint = LightBlue,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Right Copyright & Flag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = viewModel?.currentFlag ?: "🌍",
                    fontSize = 11.sp
                )
                Text(
                    text = "© 2026 JOBSREPORT",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FooterLinkItem(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = TextSecondary,
        fontSize = 11.sp,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 2.dp)
    )
}
