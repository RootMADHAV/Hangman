@file:OptIn(ExperimentalMaterial3Api::class)

package com.LetterQuest.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.LetterQuest.ui.navigation.NavigationRoute
import com.LetterQuest.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val preferences = settingsViewModel.preferences.collectAsState().value
    val tutorialSettings = settingsViewModel.tutorialSettings.collectAsState().value
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.semantics { contentDescription = "Navigate back" }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SettingItem("Sound Effects", preferences.soundEnabled) { settingsViewModel.setSoundEnabled(it) }
            Spacer(modifier = Modifier.height(16.dp))
            SettingItem("Background Music", preferences.musicEnabled) { settingsViewModel.setMusicEnabled(it) }
            Spacer(modifier = Modifier.height(16.dp))
            SettingItem("Notifications", preferences.notificationsEnabled) { settingsViewModel.setNotificationsEnabled(it) }
            Spacer(modifier = Modifier.height(16.dp))
            SettingItem("Hints & Power-ups", preferences.hintsEnabled) { settingsViewModel.setHintsEnabled(it) }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Tutorial", fontSize = 18.sp, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            SettingItem(
                title = "Show Tutorial on Next Launch",
                isEnabled = tutorialSettings.showGameplayTutorial
            ) {
                settingsViewModel.setTutorialSetting("gameplay", it)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Legal", fontSize = 18.sp, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { navController.navigate(NavigationRoute.PrivacyPolicy.route) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Privacy Policy") }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { navController.navigate(NavigationRoute.Terms.route) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Terms of Service") }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Support", fontSize = 18.sp, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val packageName = context.packageName
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                        )
                    } catch (e: ActivityNotFoundException) {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Rate App") }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    val shareText = "Download Letter Quest and challenge your vocabulary! " +
                        "https://play.google.com/store/apps/details?id=${context.packageName}"
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Letter Quest"))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Share App") }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingItem(title: String, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f), fontSize = 16.sp)
        Switch(checked = isEnabled, onCheckedChange = onToggle)
    }
}
