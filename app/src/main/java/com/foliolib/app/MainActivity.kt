package com.foliolib.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.foliolib.app.presentation.MainViewModel
import com.foliolib.app.presentation.navigation.FolioApp
import com.foliolib.app.ui.theme.FoliolibTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Request notification permission on startup
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { /* Handle permission result if needed */ }
                )

                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

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