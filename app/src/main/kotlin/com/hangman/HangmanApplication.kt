package com.hangman

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import com.hangman.domain.repository.AchievementRepository
import com.hangman.domain.repository.DailyLoginRepository
import com.hangman.ui.notification.DailyRewardNotification
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HangmanApplication : Application() {

    @Inject
    lateinit var achievementRepository: AchievementRepository

    @Inject
    lateinit var dailyLoginRepository: DailyLoginRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        initFirebase()

        applicationScope.launch {
            achievementRepository.syncAchievementCatalog()
        }
        applicationScope.launch {
            scheduleDailyRewardNotificationIfNeeded(this@HangmanApplication)
        }
    }

    private fun initFirebase() {
        try {
            Firebase.analytics.setAnalyticsCollectionEnabled(true)
            Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
        } catch (_: Exception) {
            // Fails gracefully when google-services.json contains placeholder values.
            // Replace the file with your real Firebase project config to activate.
        }
    }

    /**
     * Called from MainActivity after UMP consent is confirmed. MobileAds must
     * not be initialized before consent in regions where it is required (EEA).
     */
    fun initMobileAds() {
        MobileAds.initialize(this)
    }

    private suspend fun scheduleDailyRewardNotificationIfNeeded(context: Context) {
        try {
            val state = dailyLoginRepository.observeDailyLoginState().first()
            if (state.canClaim) {
                DailyRewardNotification.show(context)
            }
        } catch (_: Exception) {}
    }
}
