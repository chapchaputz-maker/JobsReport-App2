package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.BorderWhite5
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VioletPurple

@Composable
fun MobileNavDrawer(
    viewModel: AppViewModel,
    isOpen: Boolean
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSurface)
                .border(1.dp, BorderWhite10, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "SYSTEM NAVIGATION MENU",
                    color = LightBlue,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                // User Auth / Account item
                if (viewModel.isLoggedIn) {
                    NavItemRow(
                        title = "My Profile: ${viewModel.currentUser?.name ?: "User"}",
                        icon = Icons.Default.Person,
                        iconColor = EmeraldGreen,
                        isSelected = viewModel.currentRoute == NavRoute.USER_PROFILE,
                        onClick = {
                            viewModel.isMobileMenuOpen = false
                            viewModel.openUserProfile(null)
                        }
                    )
                } else {
                    NavItemRow(
                        title = "Sign In / Join JobsReport",
                        icon = Icons.Default.Login,
                        iconColor = PrimaryBlue,
                        isSelected = viewModel.currentRoute == NavRoute.USER_AUTH,
                        onClick = {
                            viewModel.isMobileMenuOpen = false
                            viewModel.openAuth()
                        }
                    )
                }

                NavItemRow(
                    title = "Intelligence Feed",
                    icon = Icons.Default.Home,
                    iconColor = EmeraldGreen,
                    isSelected = viewModel.currentRoute == NavRoute.INTELLIGENCE_FEED,
                    onClick = { viewModel.navigateTo(NavRoute.INTELLIGENCE_FEED) }
                )

                NavItemRow(
                    title = "Job Stories (Community)",
                    icon = Icons.Default.AutoAwesome,
                    iconColor = PrimaryBlue,
                    isSelected = viewModel.currentRoute == NavRoute.STORIES,
                    onClick = { viewModel.navigateTo(NavRoute.STORIES) }
                )

                NavItemRow(
                    title = "All Jobs List",
                    icon = Icons.Default.Work,
                    iconColor = LightBlue,
                    isSelected = viewModel.currentRoute == NavRoute.ALL_JOBS,
                    onClick = { viewModel.navigateTo(NavRoute.ALL_JOBS) }
                )

                NavItemRow(
                    title = "Saved Jobs",
                    icon = Icons.Default.Bookmark,
                    iconColor = AmberYellow,
                    isSelected = viewModel.currentRoute == NavRoute.SAVED_JOBS,
                    badgeCount = viewModel.savedJobsCount,
                    onClick = { viewModel.navigateTo(NavRoute.SAVED_JOBS) }
                )

                NavItemRow(
                    title = "Notifications",
                    icon = Icons.Default.Notifications,
                    iconColor = LightBlue,
                    isSelected = viewModel.currentRoute == NavRoute.NOTIFICATIONS,
                    badgeCount = viewModel.unreadNotificationsCount,
                    onClick = {
                        viewModel.isMobileMenuOpen = false
                        viewModel.openNotifications()
                    }
                )

                NavItemRow(
                    title = "Companies & Employers",
                    icon = Icons.Default.Business,
                    iconColor = AmberYellow,
                    isSelected = viewModel.currentRoute == NavRoute.COMPANIES,
                    onClick = { viewModel.navigateTo(NavRoute.COMPANIES) }
                )

                NavItemRow(
                    title = "Jobs by Regions",
                    icon = Icons.Default.LocationOn,
                    iconColor = VioletPurple,
                    isSelected = viewModel.currentRoute == NavRoute.REGIONS,
                    onClick = { viewModel.navigateTo(NavRoute.REGIONS) }
                )

                NavItemRow(
                    title = "All Job Reports",
                    icon = Icons.Default.Book,
                    iconColor = LightBlue,
                    isSelected = viewModel.currentRoute == NavRoute.JOB_REPORTS,
                    onClick = { viewModel.navigateTo(NavRoute.JOB_REPORTS) }
                )

                NavItemRow(
                    title = "About Us",
                    icon = Icons.Default.Info,
                    iconColor = LightBlue,
                    isSelected = viewModel.currentRoute == NavRoute.ABOUT_US,
                    onClick = { viewModel.navigateTo(NavRoute.ABOUT_US) }
                )

                NavItemRow(
                    title = "Disclaimer",
                    icon = Icons.Default.Gavel,
                    iconColor = AmberYellow,
                    isSelected = viewModel.currentRoute == NavRoute.DISCLAIMER,
                    onClick = { viewModel.navigateTo(NavRoute.DISCLAIMER) }
                )

                NavItemRow(
                    title = "Privacy Policy",
                    icon = Icons.Default.Lock,
                    iconColor = LightBlue,
                    isSelected = viewModel.currentRoute == NavRoute.PRIVACY_POLICY,
                    onClick = { viewModel.navigateTo(NavRoute.PRIVACY_POLICY) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderWhite5)
                )

                val context = LocalContext.current
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(PrimaryBlue, VioletPurple)
                            )
                        )
                        .clickable {
                            viewModel.isMobileMenuOpen = false
                            viewModel.openPostJob()
                        }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Post Job",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "POST JOB",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavItemRow(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    isSelected: Boolean,
    badgeCount: Int? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else BgCard.copy(alpha = 0.4f))
            .border(
                1.dp,
                if (isSelected) PrimaryBlue.copy(alpha = 0.3f) else BorderWhite5,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = if (isSelected) Color.White else TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (badgeCount != null && badgeCount > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AmberYellow.copy(alpha = 0.2f))
                    .border(1.dp, AmberYellow.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$badgeCount",
                    color = AmberYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
