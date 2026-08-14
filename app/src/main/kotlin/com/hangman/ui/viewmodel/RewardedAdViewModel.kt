package com.hangman.ui.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hangman.domain.model.RewardType
import com.hangman.domain.repository.TokenRepository
import com.hangman.domain.usecase.AdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI-layer bridge between Compose screens and [AdManager]. Owns all rewarded /
 * interstitial flows so GameViewModel and ShopViewModel stay untouched.
 *
 * Typical rewarded pattern from a screen:
 * ```
 * rewardedAdViewModel.loadRewardedAd(context, RewardType.COINS)
     * rewardedAdViewModel.showRewardedAd(
     *     RewardType.COINS, activity, context,
     *     onReward = { amount -> /* grant via grantTokens(amount) */ }
     * )
 * ```
 */
@HiltViewModel
class RewardedAdViewModel @Inject constructor(
    val tokenRepository: TokenRepository,
    val adManager: AdManager
) : ViewModel() {

    val adsRemoved: StateFlow<Boolean> = adManager.adsRemoved

    // ── Remove Ads IAP hook ──────────────────────────────────────────────────

    /** Mark "Remove Ads" as purchased (called after a successful billing flow). */
    fun setAdsRemoved(removed: Boolean) = adManager.setAdsRemoved(removed)

    // ── Rewarded ads ────────────────────────────────────────────────────────

    /** Async load; [onLoaded] may be useful to enable a button once ready (nullable). */
    fun loadRewardedAd(
        context: Context,
        rewardType: RewardType,
        onLoaded: () -> Unit = {}
    ) {
        adManager.loadRewardedAd(context, rewardType) { onLoaded() }
    }

    /**
     * Shows the rewarded ad previously loaded for [rewardType]. On success [onReward]
     * receives the amount from the SDK's reward API (never hardcoded here). Always
     * clears the cached ad and preloads the next one so the button stays hot.
     *
     * @return true when a cached ad was actually shown; false when the caller should
     *         surface a "still loading" hint (the preload of the next ad has started).
     */
    fun showRewardedAd(
        rewardType: RewardType,
        activity: Activity,
        context: Context,
        onReward: (amount: Int) -> Unit
    ): Boolean {
        val ad = adManager.getRewardedAd(rewardType)
        if (ad == null) {
            adManager.loadRewardedAd(context, rewardType) {}
            return false
        }
        ad.fullScreenContentCallback = emptyFullScreenCallback { adManager.clearRewardedAd(rewardType) }
        ad.show(activity) { rewardItem ->
            onReward(rewardItem.amount)
        }
        adManager.clearRewardedAd(rewardType)
        adManager.loadRewardedAd(context, rewardType) {}
        return true
    }

    /** Direct token grant used by ad rewards (uses the amount from the reward API). */
    fun grantTokens(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch { tokenRepository.earnTokens(amount) }
    }

    // ── Interstitial cadence (game-complete only, never mid-game) ───────────

    /** Called when a finished game's results screen becomes visible. */
    fun recordGameCompleted() = adManager.recordGameCompleted()

    fun shouldShowInterstitial(): Boolean = adManager.shouldShowInterstitial()

    fun resetInterstitialCounter() = adManager.resetInterstitialCounter()

    fun loadInterstitialAd(context: Context, onLoaded: () -> Unit = {}) {
        adManager.loadInterstitialAd(context) { ad -> onLoaded() }
    }

    fun showInterstitialAd(onClosed: () -> Unit = {}) = adManager.showInterstitialAd(onClosed)
}

/** Minimal FullScreenContentCallback — clears the cached ad once dismissed. */
private fun emptyFullScreenCallback(
    onDismissed: () -> Unit
): com.google.android.gms.ads.FullScreenContentCallback =
    object : com.google.android.gms.ads.FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() = onDismissed()

        override fun onAdFailedToShowFullScreenContent(
            adError: com.google.android.gms.ads.AdError
        ) = onDismissed()
    }
