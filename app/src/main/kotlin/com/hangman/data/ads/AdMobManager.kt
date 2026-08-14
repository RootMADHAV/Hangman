package com.hangman.data.ads

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.hangman.domain.model.AdConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobManager @Inject constructor(
    private val context: Context
) {
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private val _interstitialReady = MutableStateFlow(false)
    val interstitialReady: StateFlow<Boolean> = _interstitialReady

    private val _rewardedReady = MutableStateFlow(false)
    val rewardedReady: StateFlow<Boolean> = _rewardedReady

    private var gameCount = 0

    fun recordGameCompleted() {
        gameCount++
        if (gameCount % 10 == 0) {
            loadInterstitialAd()
        }
    }

    fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdConfig.INTERSTITIAL_GAME_COMPLETE,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    _interstitialReady.value = true
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    _interstitialReady.value = false
                }
            }
        )
    }

    fun showInterstitialAd(onDismissed: () -> Unit = {}) {
        interstitialAd?.let {
            it.show(context as? android.app.Activity ?: return)
            _interstitialReady.value = false
            onDismissed()
        }
    }

    fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            AdConfig.REWARDED_COINS,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _rewardedReady.value = true
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    _rewardedReady.value = false
                }
            }
        )
    }

    fun showRewardedAd(onUserEarned: (Int) -> Unit) {
        rewardedAd?.let { ad ->
            ad.show(context as? android.app.Activity ?: return) {
                onUserEarned(it.amount)
            }
            _rewardedReady.value = false
        }
    }

    fun resetGameCount() {
        gameCount = 0
    }
}
