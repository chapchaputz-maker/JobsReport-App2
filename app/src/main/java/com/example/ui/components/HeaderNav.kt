package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import coil.compose.AsyncImage
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.state.AppViewModel
import com.example.state.NavRoute
import com.example.ui.theme.AmberYellow
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgSurface
import com.example.ui.theme.BorderWhite10
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VioletPurple

@Composable
fun HeaderNav(
    viewModel: AppViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSurface.copy(alpha = 0.95f))
            .border(width = 1.dp, color = BorderWhite10)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
        // Logo & Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { viewModel.navigateTo(com.example.state.NavRoute.INTELLIGENCE_FEED) }
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(VioletPurple, PrimaryBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "JR",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "JobsReport",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = ".online",
                    color = LightBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }

        // Actions: Global icon, Menu icon, Save icon, Auth icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Country Selector (Global Icon only - no "Worldwide" text)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(8.dp))
                    .clickable { viewModel.isCountryDropdownOpen = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Country selector",
                    tint = LightBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            // 2. Mobile Menu / Hamburger Icon (Near save icon)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (viewModel.isMobileMenuOpen) PrimaryBlue.copy(alpha = 0.2f) else BgCard)
                    .border(
                        1.dp,
                        if (viewModel.isMobileMenuOpen) PrimaryBlue.copy(alpha = 0.5f) else BorderWhite10,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { viewModel.isMobileMenuOpen = !viewModel.isMobileMenuOpen },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (viewModel.isMobileMenuOpen) Icons.Default.Close else Icons.Default.Menu,
                    contentDescription = "Toggle Menu",
                    tint = if (viewModel.isMobileMenuOpen) LightBlue else TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // 3. Quick Saved Jobs Bookmark Button (Save icon)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (viewModel.currentRoute == NavRoute.SAVED_JOBS) AmberYellow.copy(alpha = 0.15f) else BgCard)
                    .border(
                        1.dp,
                        if (viewModel.currentRoute == NavRoute.SAVED_JOBS) AmberYellow.copy(alpha = 0.4f) else BorderWhite10,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { viewModel.navigateTo(NavRoute.SAVED_JOBS) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Saved Jobs",
                    tint = if (viewModel.savedJobsCount > 0) AmberYellow else TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                if (viewModel.savedJobsCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AmberYellow)
                    )
                }
            }

            // 4. Notifications Bell Button (Only shown when logged in, matching web)
            if (viewModel.isLoggedIn) {
                val unread = viewModel.unreadNotificationsCount
                val displayCount = if (unread > 99) "99+" else unread.toString()
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (viewModel.currentRoute == NavRoute.NOTIFICATIONS) PrimaryBlue.copy(alpha = 0.2f) else BgCard)
                            .border(
                                1.dp,
                                if (viewModel.currentRoute == NavRoute.NOTIFICATIONS) PrimaryBlue.copy(alpha = 0.5f) else BorderWhite10,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.openNotifications() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = if (unread > 0) "Notifications ($unread unread)" else "Notifications",
                            tint = if (viewModel.currentRoute == NavRoute.NOTIFICATIONS || unread > 0) LightBlue else TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (unread > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 5.dp, y = (-4).dp)
                                .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF3425F))
                                .border(1.5.dp, BgSurface, RoundedCornerShape(8.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayCount,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 10.sp
                            )
                        }
                    }
                }
            }

            // 5. User Account / Profile / Sign In Button (Auth icon)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (viewModel.isLoggedIn) PrimaryBlue.copy(alpha = 0.2f) else BgCard)
                    .border(
                        1.dp,
                        if (viewModel.isLoggedIn) PrimaryBlue.copy(alpha = 0.5f) else BorderWhite10,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        if (viewModel.isLoggedIn) {
                            viewModel.openUserProfile(null)
                        } else {
                            viewModel.openAuth()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.isLoggedIn && !viewModel.currentUser?.profileImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = viewModel.currentUser?.profileImageUrl,
                        contentDescription = "User Profile",
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Account Sign In",
                        tint = if (viewModel.isLoggedIn) LightBlue else TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
}
