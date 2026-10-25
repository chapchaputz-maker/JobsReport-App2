package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.state.AppViewModel
import com.example.state.NavRoute
import com.example.ui.components.AdminLoginModal
import com.example.ui.components.CountrySelectorModal
import com.example.ui.components.HeaderNav
import com.example.ui.components.MobileNavDrawer
import com.example.ui.components.NotificationBellModal
import com.example.ui.components.SearchModal
import com.example.ui.components.TechnicalFooter
import com.example.ui.screens.AboutUsScreen
import com.example.ui.screens.AdminStudioScreen
import com.example.ui.screens.CategoryScreen
import com.example.ui.screens.CompaniesScreen
import com.example.ui.screens.DisclaimerScreen
import com.example.ui.screens.IntelligenceFeedScreen
import com.example.ui.screens.JobDetailScreen
import com.example.ui.screens.JobsListScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.RegionDetailScreen
import com.example.ui.screens.RegionsScreen
import com.example.ui.screens.ReportDetailScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SavedJobsScreen
import com.example.ui.screens.UserAuthPage
import com.example.ui.theme.BgDeep

@Composable
fun JobsReportLayout(viewModel: AppViewModel) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HeaderNav(viewModel = viewModel)
        },
        bottomBar = {
            TechnicalFooter(viewModel = viewModel)
        },
        containerColor = BgDeep
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgDeep)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Navigation Overlay Drawer
                MobileNavDrawer(
                    viewModel = viewModel,
                    isOpen = viewModel.isMobileMenuOpen
                )

                // Current Route Content
                Box(modifier = Modifier.weight(1f)) {
                    when (viewModel.currentRoute) {
                        NavRoute.INTELLIGENCE_FEED -> IntelligenceFeedScreen(viewModel = viewModel)
                        NavRoute.COMPANIES -> CompaniesScreen(viewModel = viewModel)
                        NavRoute.REGIONS -> RegionsScreen(viewModel = viewModel)
                        NavRoute.ALL_JOBS -> JobsListScreen(viewModel = viewModel)
                        NavRoute.SAVED_JOBS -> SavedJobsScreen(viewModel = viewModel)
                        NavRoute.JOB_REPORTS -> ReportsScreen(viewModel = viewModel)
                        NavRoute.ADMIN_STUDIO -> AdminStudioScreen(viewModel = viewModel)
                        NavRoute.JOB_DETAIL -> JobDetailScreen(viewModel = viewModel)
                        NavRoute.CATEGORY_JOBS -> CategoryScreen(viewModel = viewModel)
                        NavRoute.REPORT_DETAIL -> ReportDetailScreen(viewModel = viewModel)
                        NavRoute.REGION_DETAIL -> RegionDetailScreen(viewModel = viewModel)
                        NavRoute.ABOUT_US -> AboutUsScreen(viewModel = viewModel)
                        NavRoute.DISCLAIMER -> DisclaimerScreen(viewModel = viewModel)
                        NavRoute.PRIVACY_POLICY -> PrivacyPolicyScreen(viewModel = viewModel)
                        NavRoute.STORIES -> com.example.ui.screens.StoryScreen(viewModel = viewModel)
                        NavRoute.USER_AUTH -> UserAuthPage(
                            viewModel = viewModel,
                            onClose = { viewModel.navigateTo(NavRoute.INTELLIGENCE_FEED) }
                        )
                        NavRoute.USER_PROFILE -> com.example.ui.screens.UserProfilePage(
                            viewModel = viewModel,
                            onClose = { viewModel.navigateTo(NavRoute.INTELLIGENCE_FEED) }
                        )
                        NavRoute.USER_JOB_INPUT -> com.example.ui.screens.UserJobInputScreen(
                            viewModel = viewModel,
                            onClose = {
                                val target = viewModel.jobInputReturnRoute ?: (if (viewModel.editingJob != null) NavRoute.USER_PROFILE else NavRoute.INTELLIGENCE_FEED)
                                viewModel.jobInputReturnRoute = null
                                viewModel.editingJob = null
                                if (target == NavRoute.USER_PROFILE) {
                                    viewModel.openUserProfile(viewModel.viewingProfileUser)
                                } else {
                                    viewModel.navigateTo(target)
                                }
                            }
                        )
                        NavRoute.NOTIFICATIONS -> com.example.ui.screens.NotificationScreen(
                            viewModel = viewModel,
                            onClose = { viewModel.navigateTo(NavRoute.INTELLIGENCE_FEED) }
                        )
                    }
                }
            }

            // Global Story Overlays & Modals (Story Viewer, Create Story)
            com.example.ui.screens.GlobalStoryModals(viewModel = viewModel)

            // Global User Profile Overlay (when triggered via modal flag)
            if (viewModel.isUserProfileModalOpen) {
                com.example.ui.screens.UserProfilePage(
                    viewModel = viewModel,
                    onClose = { viewModel.isUserProfileModalOpen = false }
                )
            }

            // Global User Auth Modal / Overlay (Login, Register, Forgot Password)
            if (viewModel.isUserAuthModalOpen) {
                UserAuthPage(
                    viewModel = viewModel,
                    onClose = { viewModel.closeAuth() }
                )
            }

            // Dialog Modals
            if (viewModel.isCountryDropdownOpen) {
                CountrySelectorModal(
                    viewModel = viewModel,
                    onDismiss = { viewModel.isCountryDropdownOpen = false }
                )
            }

            if (viewModel.isLoginModalOpen) {
                AdminLoginModal(
                    viewModel = viewModel,
                    onDismiss = { viewModel.isLoginModalOpen = false }
                )
            }

            if (viewModel.isNotificationBellOpen) {
                NotificationBellModal(
                    viewModel = viewModel,
                    onDismiss = { viewModel.isNotificationBellOpen = false }
                )
            }

            if (viewModel.isSearchModalOpen) {
                SearchModal(
                    viewModel = viewModel,
                    onDismiss = { viewModel.isSearchModalOpen = false }
                )
            }
        }
    }
}
