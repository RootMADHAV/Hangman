package com.LetterQuest

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import com.LetterQuest.domain.repository.AchievementRepository
import com.LetterQuest.domain.repository.CloudSyncRepository
import com.LetterQuest.domain.repository.DailyLoginRepository
import com.LetterQuest.domain.usecase.CloudSyncUseCase
import com.LetterQuest.ui.notification.DailyRewardNotification
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

    @Inject
    lateinit var cloudSyncUseCase: CloudSyncUseCase

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
        applicationScope.launch {
            trySync()
        }
    }

    private fun initFirebase() {
        try {
            Firebase.analytics.setAnalyticsCollectionEnabled(true)
            Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
        } catch (_: Exception) {
        }
    }

    fun initMobileAds() {
        MobileAds.initialize(this)
    }

    private suspend fun scheduleDailyRewardNotificationIfNeeded(context: Context) {
        try {
            val state = dailyLoginRepository.observeDailyLoginState().first()
            if (state.canClaim) {
                DailyRewardNotification.show(context)
            }
        } catch (e: Exception) {
            android.util.Log.w("HangmanApplication", "Failed to schedule daily reward notification", e)
        }
    }

    private suspend fun trySync() {
        try {
            cloudSyncUseCase.syncAll()
        } catch (e: Exception) {
            android.util.Log.w("HangmanApplication", "Initial sync failed", e)
        }
    }

    fun syncNow() {
        applicationScope.launch {
            try {
                cloudSyncUseCase.syncAll()
            } catch (e: Exception) {
                android.util.Log.w("HangmanApplication", "Manual sync failed", e)
            }
        }
    }
}
