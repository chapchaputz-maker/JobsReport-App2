package com.example.ui.deeplink

import android.content.Intent
import android.net.Uri
import com.example.data.JobOpportunity
import com.example.data.MarketReport
import com.example.state.AppViewModel
import com.example.state.NavRoute

/**
 * Type-safe target destination corresponding to app navigation routes.
 */
sealed class DeepLinkTarget {
    object Home : DeepLinkTarget()
    object IntelligenceFeed : DeepLinkTarget()
    object AllJobs : DeepLinkTarget()
    object SavedJobs : DeepLinkTarget()
    object Companies : DeepLinkTarget()
    object Regions : DeepLinkTarget()
    object JobReports : DeepLinkTarget()
    data class JobDetail(val jobId: String? = null) : DeepLinkTarget()
    data class CategoryJobs(val category: String? = null) : DeepLinkTarget()
    data class ReportDetail(val reportSlug: String? = null) : DeepLinkTarget()
    data class RegionDetail(val region: String? = null) : DeepLinkTarget()
    object AboutUs : DeepLinkTarget()
    object Disclaimer : DeepLinkTarget()
    object PrivacyPolicy : DeepLinkTarget()
    object Stories : DeepLinkTarget()
    object UserAuth : DeepLinkTarget()
    data class UserProfile(val userId: String? = null) : DeepLinkTarget()
    object UserJobInput : DeepLinkTarget()
    object Notifications : DeepLinkTarget()
}

/**
 * DeepLinkHandler parses incoming Android App Links (URIs)
 * and navigates to the appropriate destination in AppViewModel.
 */
object DeepLinkHandler {

    /**
     * Parses an incoming [Uri] into a typed [DeepLinkTarget].
     * Unrecognized paths fall back to [DeepLinkTarget.Home].
     */
    fun parseUri(uri: Uri?): DeepLinkTarget {
        if (uri == null) return DeepLinkTarget.Home

        val segments = uri.pathSegments ?: emptyList()
        if (segments.isEmpty()) {
            return DeepLinkTarget.Home
        }

        val first = segments[0].lowercase()
        val second = segments.getOrNull(1)

        return when (first) {
            "jobs" -> {
                val queryId = uri.getQueryParameter("id") ?: uri.getQueryParameter("jobId")
                if (!queryId.isNullOrBlank()) {
                    DeepLinkTarget.JobDetail(queryId)
                } else if (!second.isNullOrBlank()) {
                    DeepLinkTarget.JobDetail(second)
                } else {
                    DeepLinkTarget.AllJobs
                }
            }
            "job-detail", "job" -> {
                val queryId = uri.getQueryParameter("id") ?: uri.getQueryParameter("jobId") ?: second
                DeepLinkTarget.JobDetail(queryId)
            }
            "u" -> {
                // Format: /u/{userId}
                val userId = second ?: uri.getQueryParameter("id") ?: uri.getQueryParameter("userId")
                DeepLinkTarget.UserProfile(userId)
            }
            "profile" -> {
                val userId = second ?: uri.getQueryParameter("id") ?: uri.getQueryParameter("userId")
                DeepLinkTarget.UserProfile(userId)
            }
            "post-job" -> DeepLinkTarget.UserJobInput
            "stories", "story" -> DeepLinkTarget.Stories
            "market" -> {
                // Format: /market/{slug}
                if (!second.isNullOrBlank()) {
                    DeepLinkTarget.ReportDetail(second)
                } else {
                    DeepLinkTarget.JobReports
                }
            }
            "companies", "company" -> DeepLinkTarget.Companies
            "reports" -> {
                if (!second.isNullOrBlank()) {
                    DeepLinkTarget.ReportDetail(second)
                } else {
                    DeepLinkTarget.JobReports
                }
            }
            "report-detail" -> {
                val slug = second ?: uri.getQueryParameter("slug") ?: uri.getQueryParameter("id")
                DeepLinkTarget.ReportDetail(slug)
            }
            "region" -> {
                val regionSlug = second ?: uri.getQueryParameter("slug") ?: uri.getQueryParameter("name")
                DeepLinkTarget.RegionDetail(regionSlug)
            }
            "region-detail" -> {
                val regionSlug = second ?: uri.getQueryParameter("name") ?: uri.getQueryParameter("slug")
                DeepLinkTarget.RegionDetail(regionSlug)
            }
            "regions" -> {
                if (!second.isNullOrBlank()) {
                    DeepLinkTarget.RegionDetail(second)
                } else {
                    DeepLinkTarget.Regions
                }
            }
            "category" -> {
                val catSlug = second ?: uri.getQueryParameter("category") ?: uri.getQueryParameter("slug") ?: uri.getQueryParameter("name")
                DeepLinkTarget.CategoryJobs(catSlug)
            }
            "category-jobs" -> {
                val catSlug = second ?: uri.getQueryParameter("category") ?: uri.getQueryParameter("name")
                DeepLinkTarget.CategoryJobs(catSlug)
            }
            "saved-jobs" -> DeepLinkTarget.SavedJobs
            "notifications" -> DeepLinkTarget.Notifications
            "about-us", "about" -> DeepLinkTarget.AboutUs
            "disclaimer" -> DeepLinkTarget.Disclaimer
            "privacy-policy", "privacy" -> DeepLinkTarget.PrivacyPolicy
            "auth", "login", "signin", "signup" -> DeepLinkTarget.UserAuth
            else -> DeepLinkTarget.Home
        }
    }

    /**
     * Applies the parsed target directly to the [AppViewModel].
     */
    fun applyTarget(target: DeepLinkTarget, viewModel: AppViewModel) {
        when (target) {
            is DeepLinkTarget.Home,
            is DeepLinkTarget.IntelligenceFeed -> {
                viewModel.navigateTo(NavRoute.INTELLIGENCE_FEED)
            }
            is DeepLinkTarget.AllJobs -> {
                viewModel.navigateTo(NavRoute.ALL_JOBS)
            }
            is DeepLinkTarget.SavedJobs -> {
                viewModel.navigateTo(NavRoute.SAVED_JOBS)
            }
            is DeepLinkTarget.Companies -> {
                viewModel.navigateTo(NavRoute.COMPANIES)
            }
            is DeepLinkTarget.Regions -> {
                viewModel.navigateTo(NavRoute.REGIONS)
            }
            is DeepLinkTarget.JobReports -> {
                viewModel.navigateTo(NavRoute.JOB_REPORTS)
            }
            is DeepLinkTarget.JobDetail -> {
                val id = target.jobId
                if (!id.isNullOrBlank()) {
                    val existing = viewModel.activeJobs.find { it.id == id }
                        ?: viewModel.savedJobs.find { it.id == id }
                    if (existing != null) {
                        viewModel.selectJob(existing)
                    } else {
                        val placeholder = JobOpportunity(
                            id = id,
                            title = "Job Details",
                            company = "JobsReport"
                        )
                        viewModel.selectJob(placeholder)
                    }
                } else {
                    viewModel.navigateTo(NavRoute.ALL_JOBS)
                }
            }
            is DeepLinkTarget.CategoryJobs -> {
                val cat = target.category
                if (!cat.isNullOrBlank()) {
                    val cleanCategory = cat.replace("-", " ").trim()
                    viewModel.selectCategory(cleanCategory)
                } else {
                    viewModel.navigateTo(NavRoute.ALL_JOBS)
                }
            }
            is DeepLinkTarget.ReportDetail -> {
                val slug = target.reportSlug
                if (!slug.isNullOrBlank()) {
                    val existing = viewModel.reports.find { it.slug == slug || it.id == slug }
                    if (existing != null) {
                        viewModel.selectReport(existing)
                    } else {
                        val fallback = MarketReport(
                            id = slug,
                            slug = slug,
                            title = slug.replace("-", " ").replaceFirstChar { it.uppercase() }
                        )
                        viewModel.selectReport(fallback)
                    }
                } else {
                    viewModel.navigateTo(NavRoute.JOB_REPORTS)
                }
            }
            is DeepLinkTarget.RegionDetail -> {
                val reg = target.region
                if (!reg.isNullOrBlank()) {
                    val cleanRegion = reg.replace("-", " ").trim()
                    viewModel.selectRegion(cleanRegion)
                } else {
                    viewModel.navigateTo(NavRoute.REGIONS)
                }
            }
            is DeepLinkTarget.AboutUs -> {
                viewModel.navigateTo(NavRoute.ABOUT_US)
            }
            is DeepLinkTarget.Disclaimer -> {
                viewModel.navigateTo(NavRoute.DISCLAIMER)
            }
            is DeepLinkTarget.PrivacyPolicy -> {
                viewModel.navigateTo(NavRoute.PRIVACY_POLICY)
            }
            is DeepLinkTarget.Stories -> {
                viewModel.navigateTo(NavRoute.STORIES)
            }
            is DeepLinkTarget.UserAuth -> {
                viewModel.navigateTo(NavRoute.USER_AUTH)
            }
            is DeepLinkTarget.UserProfile -> {
                val userId = target.userId
                if (!userId.isNullOrBlank()) {
                    viewModel.openUserProfileById(userId)
                } else {
                    viewModel.openUserProfile(null)
                }
            }
            is DeepLinkTarget.UserJobInput -> {
                viewModel.openPostJob()
            }
            is DeepLinkTarget.Notifications -> {
                viewModel.openNotifications()
            }
        }
    }

    /**
     * Handles an incoming intent:
     * - Safely ignores intents with no data (normal app launch without crashing).
     * - Parses and routes the intent if valid.
     *
     * @return true if an App Link deep link was successfully handled, false otherwise.
     */
    fun handle(intent: Intent?, viewModel: AppViewModel): Boolean {
        val data = intent?.data ?: return false
        val target = parseUri(data)
        applyTarget(target, viewModel)
        return true
    }
}
