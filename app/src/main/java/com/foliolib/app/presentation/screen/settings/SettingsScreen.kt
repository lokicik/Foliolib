package com.foliolib.app.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.material.icons.filled.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showPermissionRationale by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                pendingAction?.invoke()
                pendingAction = null
            } else {
                // If permission denied, check if we should show rationale or if it's permanently denied
                // Note: In Compose, we don't have direct access to shouldShowRequestPermissionRationale easily without Activity
                // But we can infer it or just show a dialog explaining why we need it
                showPermissionRationale = true
            }
        }
    )

    fun checkAndRequestPermission(onGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    onGranted()
                }
                else -> {
                    pendingAction = onGranted
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            onGranted()
        }
    }

    // Initialize default shelves on first launch
    LaunchedEffect(Unit) {
        viewModel.initializeDefaultShelves()
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Notification Permission Required") },
            text = { Text("Foliolib needs notification permission to send reading reminders and export notifications. Please enable it in settings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationale = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Dark Theme",
                    subtitle = if (uiState.isDarkTheme) "Enabled" else "Disabled",
                    trailing = {
                        Switch(
                            checked = uiState.isDarkTheme,
                            onCheckedChange = { viewModel.toggleTheme() }
                        )
                    }
                )

                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = when (uiState.currentLanguage) {
                        "es" -> "Español"
                        "tr" -> "Türkçe"
                        else -> "English"
                    },
                    onClick = { viewModel.showLanguageDialog() }
                )
            }

            Divider()

            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Reading Reminders",
                    subtitle = "Daily notifications to maintain your streak",
                    trailing = {
                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { 
                                if (!uiState.notificationsEnabled) {
                                    checkAndRequestPermission {
                                        viewModel.toggleNotifications()
                                    }
                                } else {
                                    viewModel.toggleNotifications()
                                }
                            }
                        )
                    }
                )

                if (uiState.notificationsEnabled) {
                    SettingsItem(
                        icon = Icons.Default.Schedule,
                        title = "Reminder Time",
                        subtitle = String.format("%02d:%02d", uiState.reminderHour, uiState.reminderMinute),
                        onClick = { viewModel.showTimePicker() }
                    )
                }
            }

            Divider()

            // Data Section
            SettingsSection(title = "Data") {
                SettingsItem(
                    icon = Icons.Default.Upload,
                    title = "Export Library",
                    subtitle = "Export your books to CSV/JSON",
                    onClick = { viewModel.showDataExportDialog() }
                )
            }

            Divider()

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About Foliolib",
                    subtitle = "Version ${uiState.appVersion}",
                    onClick = { viewModel.showAboutDialog() }
                )

                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Privacy Policy",
                    subtitle = "How we handle your data"
                )

                SettingsItem(
                    icon = Icons.Default.Gavel,
                    title = "Terms of Service",
                    subtitle = "Terms and conditions"
                )
            }
        }

        // Language Dialog
        if (uiState.showLanguageDialog) {
            LanguageDialog(
                currentLanguage = uiState.currentLanguage,
                onLanguageSelected = { viewModel.setLanguage(it) },
                onDismiss = { viewModel.hideLanguageDialog() }
            )
        }

        // About Dialog
        if (uiState.showAboutDialog) {
            AboutDialog(
                version = uiState.appVersion,
                onDismiss = { viewModel.hideAboutDialog() }
            )
        }

        // Time Picker Dialog
        if (uiState.showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = uiState.reminderHour,
                initialMinute = uiState.reminderMinute
            )

            AlertDialog(
                onDismissRequest = { viewModel.hideTimePicker() },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.updateReminderTime(timePickerState.hour, timePickerState.minute)
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideTimePicker() }) {
                        Text("Cancel")
                    }
                },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }

        // Export Dialog
        if (uiState.showDataExportDialog) {
            DataExportDialog(
                isExporting = uiState.isExporting,
                exportSuccess = uiState.exportSuccess,
                exportFormat = uiState.exportFormat,
                onFormatChange = { viewModel.setExportFormat(it) },
                onExport = { 
                    checkAndRequestPermission {
                        viewModel.exportData() 
                    }
                },
                onDismiss = { viewModel.hideDataExportDialog() }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        content()
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        trailing?.invoke()
    }
}

@Composable
private fun LanguageDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language") },
        text = {
            Column {
                LanguageItem(
                    name = "English",
                    code = "en",
                    isSelected = currentLanguage == "en",
                    onSelect = onLanguageSelected
                )
                LanguageItem(
                    name = "Español",
                    code = "es",
                    isSelected = currentLanguage == "es",
                    onSelect = onLanguageSelected
                )
                LanguageItem(
                    name = "Türkçe",
                    code = "tr",
                    isSelected = currentLanguage == "tr",
                    onSelect = onLanguageSelected
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun LanguageItem(
    name: String,
    code: String,
    isSelected: Boolean,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(code) }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelect(code) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun AboutDialog(
    version: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text("📚", style = MaterialTheme.typography.displayMedium)
        },
        title = { Text("Foliolib") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Version $version")
                Text(
                    "A beautifully designed book management app for tracking your reading journey.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Made with ❤️ using Jetpack Compose",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DataExportDialog(
    isExporting: Boolean,
    exportSuccess: Boolean,
    exportFormat: ExportFormat,
    onFormatChange: (ExportFormat) -> Unit,
    onExport: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Library") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    isExporting -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Text("Exporting your library...")
                        }
                    }
                    exportSuccess -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Export completed successfully!")
                        }
                    }
                    else -> {
                        Text("Export your entire library to a file. This includes all books, shelves, reading sessions, and notes.")
                        Text(
                            "Choose format:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = exportFormat == ExportFormat.JSON,
                                onClick = { onFormatChange(ExportFormat.JSON) }
                            )
                            Text(
                                text = "JSON",
                                modifier = Modifier
                                    .clickable { onFormatChange(ExportFormat.JSON) }
                                    .padding(end = 16.dp)
                            )
                            
                            RadioButton(
                                selected = exportFormat == ExportFormat.CSV,
                                onClick = { onFormatChange(ExportFormat.CSV) }
                            )
                            Text(
                                text = "CSV",
                                modifier = Modifier.clickable { onFormatChange(ExportFormat.CSV) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isExporting && !exportSuccess) {
                TextButton(onClick = onExport) {
                    Text("Export")
                }
            } else if (exportSuccess) {
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (!isExporting && !exportSuccess) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
