package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.state.AppViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LoginRestartCrashTest {

    @Test
    fun testLoginAndRestartApp() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = app.getSharedPreferences("jobsreport_prefs", Context.MODE_PRIVATE)

        // Simulate saved login in prefs as done by updateCurrentUser
        // Let's create a valid dummy JWT with future exp
        val header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
        val payload = "eyJzdWIiOiJ1c3JfMTIzNCIsImVtYWlsIjoidGVzdEBleGFtcGxlLmNvbSIsImV4cCI6OTk5OTk5OTk5OX0"
        val signature = "dummy_sig"
        val dummyJwt = "$header.$payload.$signature"

        prefs.edit()
            .putString("jobsreport_token", dummyJwt)
            .putString("user_id", "usr_1234")
            .putString("user_name", "Test User")
            .putString("user_username", "testuser")
            .putString("user_email", "test@example.com")
            .putString("user_avatar", "https://example.com/avatar.png")
            .putString("user_cover", "https://example.com/cover.png")
            .putString("user_headline", "Software Engineer")
            .putString("user_location", "Dar es Salaam")
            .putString("user_bio", "Bio text")
            .putString("user_work", "Tech Corp")
            .putString("user_education", "University")
            .putString("user_website", "https://example.com")
            .putInt("user_is_verified", 1)
            .putInt("user_followers", 10)
            .putInt("user_following", 5)
            .putInt("user_jobs_count", 2)
            .putInt("user_stories_count", 1)
            .putInt("user_job_adverts", 0)
            .apply()

        // Launch MainActivity
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull(activity)
    }
}
