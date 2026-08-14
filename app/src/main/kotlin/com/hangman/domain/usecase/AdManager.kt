package com.hangman.domain.usecase

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.hangman.domain.model.AdConfig
import com.hangman.domain.model.RewardType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor() {
    private val _adsRemoved = MutableStateFlow(false)
    val adsRemoved: StateFlow<Boolean> = _adsRemoved.asStateFlow()

    private val _gamesCompletedSinceInterstitial = MutableStateFlow(0)
    val gamesCompletedSinceInterstitial: StateFlow<Int> = _gamesCompletedSinceInterstitial.asStateFlow()

    private var interstitialAd: InterstitialAd? = null
    private var hintsRewardedAd: RewardedAd? = null
    private var coinsRewardedAd: RewardedAd? = null

    fun setAdsRemoved(removed: Boolean) {
        _adsRemoved.value = removed
    }

    fun recordGameCompleted() {
        val current = _gamesCompletedSinceInterstitial.value
        _gamesCompletedSinceInterstitial.value = current + 1
    }

    fun shouldShowInterstitial(): Boolean {
        return _gamesCompletedSinceInterstitial.value >= 10 && !_adsRemoved.value
    }

    fun resetInterstitialCounter() {
        _gamesCompletedSinceInterstitial.value = 0
    }

    fun loadInterstitialAd(context: Context, onLoaded: (InterstitialAd?) -> Unit) {
        if (_adsRemoved.value) {
            onLoaded(null)
            return
        }

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdConfig.INTERSTITIAL_GAME_COMPLETE,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    onLoaded(ad)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    onLoaded(null)
                }
            }
        )
    }

    fun showInterstitialAd(onClosed: () -> Unit) {
        interstitialAd?.let {
            // Show ad and reset counter
            resetInterstitialCounter()
            onClosed()
            interstitialAd = null
        }
    }

    fun loadRewardedAd(context: Context, rewardType: RewardType, onLoaded: (RewardedAd?) -> Unit) {
        if (_adsRemoved.value) {
            onLoaded(null)
            return
        }

        val adUnitId = when (rewardType) {
            RewardType.HINT -> AdConfig.REWARDED_HINTS
            RewardType.COINS -> AdConfig.REWARDED_COINS
        }

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    when (rewardType) {
                        RewardType.HINT -> hintsRewardedAd = ad
                        RewardType.COINS -> coinsRewardedAd = ad
                    }
                    onLoaded(ad)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    when (rewardType) {
                        RewardType.HINT -> hintsRewardedAd = null
                        RewardType.COINS -> coinsRewardedAd = null
                    }
                    onLoaded(null)
                }
            }
        )
    }

    fun getRewardedAd(rewardType: RewardType): RewardedAd? {
        return when (rewardType) {
            RewardType.HINT -> hintsRewardedAd
            RewardType.COINS -> coinsRewardedAd
        }
    }

    fun clearRewardedAd(rewardType: RewardType) {
        when (rewardType) {
            RewardType.HINT -> hintsRewardedAd = null
            RewardType.COINS -> coinsRewardedAd = null
        }
    }
}
