package com.foliolib.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.foliolib.app.presentation.MainViewModel
import com.foliolib.app.presentation.navigation.FolioApp
import com.foliolib.app.ui.theme.FoliolibTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by mainViewModel.themeMode.collectAsState()
            val isSystemDarkTheme = isSystemInDarkTheme()

            // Use explicit dark theme if set, otherwise fall back to system
            val effectiveDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemDarkTheme
            }

            FoliolibTheme(darkTheme = effectiveDarkTheme) {
                FolioApp()
            }
        }
    }
}