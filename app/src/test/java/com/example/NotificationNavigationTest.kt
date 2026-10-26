package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.AppNotification
import com.example.data.NotificationActor
import com.example.data.formatNotificationDisplay
import com.example.state.AppViewModel
import com.example.state.NavRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationNavigationTest {

    @Test
    fun testNotificationDisplay_showsActorName_forStories() {
        // Case 1: title is "shared new Job story", actor is Jastin Beda
        val notif1 = AppNotification(
            id = "notif_1",
            type = "story_created",
            title = "shared new Job story",
            actor = NotificationActor(id = "user_jastin", name = "Jastin Beda")
        )
        val (actor1, title1) = formatNotificationDisplay(notif1)
        assertEquals("Jastin Beda", actor1)
        assertEquals("Jastin Beda shared new Job story", title1)

        // Case 2: title with typo as in user prompt "shared new Job stoty"
        val notif2 = AppNotification(
            id = "notif_2",
            type = "story_created",
            title = "shared new Job stoty",
            actor = NotificationActor(id = "user_jastin", name = "Jastin beda")
        )
        val (actor2, title2) = formatNotificationDisplay(notif2)
        assertEquals("Jastin beda", actor2)
        assertEquals("Jastin beda shared new Job stoty", title2)

        // Case 3: title is generic "New Job Story"
        val notif3 = AppNotification(
            id = "notif_3",
            type = "story_created",
            title = "New Job Story",
            actor = NotificationActor(id = "user_jastin", name = "Jastin Beda")
        )
        val (actor3, title3) = formatNotificationDisplay(notif3)
        assertEquals("Jastin Beda", actor3)
        assertEquals("Jastin Beda shared new Job story", title3)
    }

    @Test
    fun testNotificationDisplay_showsActorName_forJobs() {
        val notif = AppNotification(
            id = "notif_job",
            type = "job_created",
            title = "Senior Android Developer",
            actor = NotificationActor(id = "comp_1", name = "TechCorp Tanzania")
        )
        val (actor, title) = formatNotificationDisplay(notif)
        assertEquals("TechCorp Tanzania", actor)
        assertTrue(title.contains("TechCorp Tanzania"))
        assertTrue(title.contains("Senior Android Developer"))
    }

    @Test
    fun testHandleNotificationClick_storyNotification_opensStoryView() {
        val viewModel = AppViewModel(ApplicationProvider.getApplicationContext())

        val storyNotif = AppNotification(
            id = "story_notif_100",
            type = "story_created",
            title = "shared new Job story",
            body = "Just landed a Senior Engineer position!",
            actorId = "user_jastin",
            actor = NotificationActor(id = "user_jastin", name = "Jastin Beda"),
            entityType = "story",
            entityId = "story_target_999"
        )

        viewModel.handleNotificationClick(storyNotif)

        // Verify that story viewer is activated and route is STORIES
        assertEquals(NavRoute.STORIES, viewModel.currentRoute)
        assertNotNull(viewModel.viewingStory)
        assertEquals("story_target_999", viewModel.viewingStory?.id)
        assertEquals("Jastin Beda", viewModel.viewingStory?.user?.name)
    }

    @Test
    fun testHandleNotificationClick_jobNotification_opensJobDetails() {
        val viewModel = AppViewModel(ApplicationProvider.getApplicationContext())

        val jobNotif = AppNotification(
            id = "job_notif_200",
            type = "job_created",
            title = "Cloud Infrastructure Engineer",
            body = "Full-time remote role with competitive package",
            actor = NotificationActor(id = "comp_2", name = "CloudGlobal"),
            entityType = "job",
            entityId = "job_target_888"
        )

        viewModel.handleNotificationClick(jobNotif)

        // Verify that JobDetail is activated and selectedJob is populated
        assertEquals(NavRoute.JOB_DETAIL, viewModel.currentRoute)
        assertNotNull(viewModel.selectedJob)
        assertEquals("job_target_888", viewModel.selectedJob?.id)
        assertTrue(viewModel.selectedJob?.title?.contains("Cloud Infrastructure Engineer") == true)
        assertEquals("CloudGlobal", viewModel.selectedJob?.company)
    }
}
