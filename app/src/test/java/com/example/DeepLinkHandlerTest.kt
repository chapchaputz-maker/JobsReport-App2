package com.example

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.state.AppViewModel
import com.example.state.NavRoute
import com.example.ui.deeplink.DeepLinkHandler
import com.example.ui.deeplink.DeepLinkTarget
import com.example.ui.deeplink.LinkVerifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeepLinkHandlerTest {

    @Test
    fun testParseUri_variousRoutes() {
        // Home
        val homeTarget = DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/"))
        assertEquals(DeepLinkTarget.Home, homeTarget)

        // Jobs
        val allJobsTarget = DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/jobs"))
        assertEquals(DeepLinkTarget.AllJobs, allJobsTarget)

        // Job Detail via query
        val jobDetailQuery = DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/job-detail?id=job_123"))
        assertTrue(jobDetailQuery is DeepLinkTarget.JobDetail)
        assertEquals("job_123", (jobDetailQuery as DeepLinkTarget.JobDetail).jobId)

        // Job Detail via path
        val jobDetailPath = DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/jobs/job_456"))
        assertTrue(jobDetailPath is DeepLinkTarget.JobDetail)
        assertEquals("job_456", (jobDetailPath as DeepLinkTarget.JobDetail).jobId)

        // User profile /u/{userId}
        val userTarget = DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/u/usr_789"))
        assertTrue(userTarget is DeepLinkTarget.UserProfile)
        assertEquals("usr_789", (userTarget as DeepLinkTarget.UserProfile).userId)

        // Category /category/{slug}
        val catTarget = DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/category/tech-engineering"))
        assertTrue(catTarget is DeepLinkTarget.CategoryJobs)
        assertEquals("tech-engineering", (catTarget as DeepLinkTarget.CategoryJobs).category)

        // Region /region/{slug}
        val regionTarget = DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/region/dar-es-salaam"))
        assertTrue(regionTarget is DeepLinkTarget.RegionDetail)
        assertEquals("dar-es-salaam", (regionTarget as DeepLinkTarget.RegionDetail).region)

        // Market /market/{slug} for a report
        val marketTarget = DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/market/q3-report"))
        assertTrue(marketTarget is DeepLinkTarget.ReportDetail)
        assertEquals("q3-report", (marketTarget as DeepLinkTarget.ReportDetail).reportSlug)

        // Static routes
        assertEquals(DeepLinkTarget.SavedJobs, DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/saved-jobs")))
        assertEquals(DeepLinkTarget.Notifications, DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/notifications")))
        assertEquals(DeepLinkTarget.AboutUs, DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/about-us")))
        assertEquals(DeepLinkTarget.Stories, DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/stories")))
        assertEquals(DeepLinkTarget.UserJobInput, DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/post-job")))
        assertEquals(DeepLinkTarget.UserAuth, DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/auth")))

        // Unknown route falls back to Home
        val unknownTarget = DeepLinkHandler.parseUri(Uri.parse("https://jobsreport.online/unmapped-path-12345"))
        assertEquals(DeepLinkTarget.Home, unknownTarget)
    }

    @Test
    fun testJobDeepLink_accountantJobMarketUrl() {
        val uri = Uri.parse("https://jobsreport.online/market/accountant-job-mw61pjcs3qji")
        val target = DeepLinkHandler.parseUri(uri)

        // Must parse to JobDetail and preserve exact identifier
        assertTrue("Expected DeepLinkTarget.JobDetail", target is DeepLinkTarget.JobDetail)
        val jobDetail = target as DeepLinkTarget.JobDetail
        assertEquals("accountant-job-mw61pjcs3qji", jobDetail.jobId)

        // Applying target routes to JobDetail screen
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = AppViewModel(app)
        DeepLinkHandler.applyTarget(jobDetail, vm)
        assertEquals(NavRoute.JOB_DETAIL, vm.currentRoute)
    }

    @Test
    fun testStoryDeepLink_storyImageSpecificUrl() {
        val uri = Uri.parse("https://jobsreport.online/story/story-image-mw62de")
        val target = DeepLinkHandler.parseUri(uri)

        // Must parse to StoryDetail with exact story slug
        assertTrue("Expected DeepLinkTarget.StoryDetail", target is DeepLinkTarget.StoryDetail)
        val storyDetail = target as DeepLinkTarget.StoryDetail
        assertEquals("story-image-mw62de", storyDetail.storySlug)

        // Applying target initiates navigation to STORIES
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = AppViewModel(app)
        DeepLinkHandler.applyTarget(storyDetail, vm)
        assertEquals(NavRoute.STORIES, vm.currentRoute)
    }

    @Test
    fun testHandle_nullIntentOrData() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = AppViewModel(app)

        assertFalse(DeepLinkHandler.handle(null, vm))
        assertFalse(DeepLinkHandler.handle(Intent(), vm))
    }

    @Test
    fun testHandle_appliesRouteToViewModel() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = AppViewModel(app)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jobsreport.online/saved-jobs"))
        val handled = DeepLinkHandler.handle(intent, vm)

        assertTrue(handled)
        assertEquals(NavRoute.SAVED_JOBS, vm.currentRoute)
    }

    @Test
    fun testLinkVerifier_statusDoesNotCrash() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val status = LinkVerifier.getVerificationStatus(app, "jobsreport.online")
        assertTrue(status in listOf("verified", "unverified", "unknown"))
    }
}
