package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.state.AppViewModel
import com.example.ui.JobsReportLayout
import com.example.ui.deeplink.DeepLinkHandler
import com.example.ui.theme.JobsReportTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle incoming deep link or cold app launch safely
        DeepLinkHandler.handle(intent, viewModel)

        setContent {
            JobsReportTheme {
                JobsReportLayout(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle incoming deep links arriving while app is already running
        DeepLinkHandler.handle(intent, viewModel)
    }
}
