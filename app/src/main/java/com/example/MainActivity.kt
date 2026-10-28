package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.state.AppViewModel
import com.example.ui.JobsReportLayout
import com.example.ui.deeplink.DeepLinkHandler
import com.example.ui.theme.JobsReportTheme
import com.example.update.InAppUpdateManager
import com.example.update.InAppUpdatePrompt

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()
    private val updateManager by lazy { InAppUpdateManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle incoming deep link or cold app launch safely
        DeepLinkHandler.handle(intent, viewModel)

        // Check for official Google Play in-app updates
        updateManager.checkForUpdate()

        setContent {
            JobsReportTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    JobsReportLayout(viewModel = viewModel)
                    InAppUpdatePrompt(updateManager = updateManager)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateManager.onResume(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        updateManager.unregisterListener()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle incoming deep links arriving while app is already running
        DeepLinkHandler.handle(intent, viewModel)
    }
}
