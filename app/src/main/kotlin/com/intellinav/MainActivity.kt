package com.intellinav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.intellinav.ui.MainViewModel
import com.intellinav.ui.navigation.IntelliNavAppRoot
import com.intellinav.ui.theme.IntelliNavTheme

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      IntelliNavTheme {
        IntelliNavAppRoot(viewModel = viewModel)
      }
    }
  }
}
