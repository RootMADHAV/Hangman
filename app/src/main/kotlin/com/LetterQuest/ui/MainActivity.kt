package com.LetterQuest.ui

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.LetterQuest.HangmanApplication
import com.LetterQuest.data.consent.ConsentManager
import com.LetterQuest.domain.repository.PreferencesRepository
import com.LetterQuest.domain.repository.AuthRepository
import com.LetterQuest.domain.usecase.AdManager
import com.LetterQuest.domain.usecase.MusicPlayer
import com.LetterQuest.ui.navigation.NavGraph
import com.LetterQuest.ui.navigation.NavigationRoute
import com.LetterQuest.ui.theme.HangmanGameTheme
import com.LetterQuest.ui.viewmodel.SoundViewModel
import com.LetterQuest.ui.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var consentManager: ConsentManager

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var authRepository: com.LetterQuest.domain.repository.AuthRepository

    @Inject
    lateinit var musicPlayer: com.LetterQuest.domain.usecase.MusicPlayer

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
        lifecycleScope.launch {
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

        lifecycleScope.launch {
            try {
                val prefsResult = preferencesRepository.getPreferences().getOrNull()
                val adsRemoved = prefsResult?.adsRemoved ?: false
                adManager.setAdsRemoved(adsRemoved)
            } catch (e: Exception) {
                adManager.setAdsRemoved(false)
            }
        }

        var startDestination by mutableStateOf(NavigationRoute.Auth.route)

        lifecycleScope.launch {
            val authState = authRepository.currentUser.first()
            val tutorials = preferencesRepository.getTutorialSettings().getOrNull()
            val showTutorial = tutorials?.showGameplayTutorial == true && authState !is com.LetterQuest.domain.model.AuthState.Authenticated

            startDestination = when {
                showTutorial -> NavigationRoute.Tutorial.route
                authState is com.LetterQuest.domain.model.AuthState.Authenticated -> NavigationRoute.Home.route
                else -> NavigationRoute.Auth.route
            }
        }

        setContent {
            MainApp(
                startDestination = startDestination,
                showRatePrompt = showRatePrompt
            )
        }
    }

    override fun onStart() {
        super.onStart()
        musicPlayer.start()
    }

    override fun onStop() {
        super.onStop()
        musicPlayer.pause()
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

    LaunchedEffect(startDestination) {
        if (startDestination != NavigationRoute.Auth.route) {
            navController.navigate(startDestination) {
                popUpTo(NavigationRoute.Auth.route) { inclusive = true }
            }
        }
    }

    // Create SoundViewModel at the app level so BGM starts immediately
    // and keeps playing across all screens.
    val soundViewModel: SoundViewModel = hiltViewModel()

    HangmanGameTheme(
        darkTheme = isDarkTheme,
        customColors = customColors,
        customIsDark = customIsDark
    ) {
        NavGraph(navController, startDestination = NavigationRoute.Auth.route, soundViewModel = soundViewModel)

        if (showRateDialog) {
            AlertDialog(
                onDismissRequest = { showRateDialog = false },
                title = { Text("Enjoying Letter Quest?") },
                text = { Text("Enjoying Letter Quest? Rate us!") },
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
