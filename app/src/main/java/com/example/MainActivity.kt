package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.state.AppViewModel
import com.example.ui.JobsReportLayout
import com.example.ui.theme.JobsReportTheme

class MainActivity : ComponentActivity() {
  private val viewModel: AppViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      JobsReportTheme {
        JobsReportLayout(viewModel = viewModel)
      }
    }
  }
}

