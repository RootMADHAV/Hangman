package com.hangman.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.hangman.HangmanApplication
import com.hangman.data.consent.ConsentManager
import com.hangman.domain.repository.PreferencesRepository
import com.hangman.ui.navigation.NavGraph
import com.hangman.ui.navigation.NavigationRoute
import com.hangman.ui.theme.HangmanGameTheme
import com.hangman.ui.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var consentManager: ConsentManager

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()
        requestNotificationPermissionIfNeeded()

        // Request UMP consent; MobileAds initializes only after consent is confirmed.
        consentManager.requestConsent(this) { canShowAds ->
            if (canShowAds) {
                (application as HangmanApplication).initMobileAds()
            }
        }

        // Track launch count; first launch no longer force-navigates to Tutorial.
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val launchCount = prefs.getInt("launch_count", 0) + 1
        prefs.edit()
            .putInt("launch_count", launchCount)
            .putBoolean("is_first_launch", false)
            .apply()
        val showRatePrompt = launchCount >= 10

        // If the player left "Show Tutorial" switched on in Settings, redirect the
        // first screen to the tutorial and immediately disarm the flag. This way it
        // shows exactly once per toggle without needing a crash-only path.
        kotlinx.coroutines.runBlocking {
            try {
                val tutorials = preferencesRepository.getTutorialSettings().getOrNull()
                if (tutorials?.showGameplayTutorial == true) {
                    prefs.edit().putBoolean("show_tutorial_on_launch", true).apply()
                    preferencesRepository.setTutorialSeen("gameplay")
                } else {
                    prefs.edit().putBoolean("show_tutorial_on_launch", false).apply()
                }
            } catch (e: Exception) {
                prefs.edit().putBoolean("show_tutorial_on_launch", false).apply()
            }
        }
        val startDestination = if (prefs.getBoolean("show_tutorial_on_launch", false)) {
            NavigationRoute.Tutorial.route
        } else {
            NavigationRoute.Home.route
        }

        setContent {
            MainApp(
                startDestination = startDestination,
                showRatePrompt = showRatePrompt
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DAILY_REWARD_CHANNEL_ID,
                "Daily Reward",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies you when your daily login reward is ready to claim"
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    companion object {
        const val DAILY_REWARD_CHANNEL_ID = "daily_reward"
    }
}

@Composable
private fun MainApp(startDestination: String, showRatePrompt: Boolean) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val isDarkTheme = themeViewModel.isDarkTheme.collectAsState().value
    val customColors = themeViewModel.customColors.collectAsState().value
    val customIsDark = themeViewModel.customIsDark.collectAsState().value
    val navController = rememberNavController()
    var showRateDialog by remember { mutableStateOf(showRatePrompt) }

    HangmanGameTheme(
        darkTheme = isDarkTheme,
        customColors = customColors,
        customIsDark = customIsDark
    ) {
        NavGraph(navController, startDestination = startDestination)

        if (showRateDialog) {
            AlertDialog(
                onDismissRequest = { showRateDialog = false },
                title = { Text("Enjoying Hangman?") },
                text = { Text("Enjoying Hangman? Rate us!") },
                confirmButton = {
                    TextButton(onClick = {
                        showRateDialog = false
                        navController.navigate(NavigationRoute.Settings.route)
                    }) { Text("Rate Now") }
                },
                dismissButton = {
                    TextButton(onClick = { showRateDialog = false }) { Text("Later") }
                }
            )
        }
    }
}
