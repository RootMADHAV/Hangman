package com.LetterQuest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LetterQuest.domain.model.DailyLoginReward
import com.LetterQuest.domain.model.InAppPurchase
import com.LetterQuest.domain.model.InAppPurchases
import com.LetterQuest.domain.model.ShopItem
import com.LetterQuest.domain.model.UserTokens
import com.LetterQuest.domain.repository.BillingRepository
import com.LetterQuest.domain.repository.DailyLoginRepository
import com.LetterQuest.domain.repository.PreferencesRepository
import com.LetterQuest.domain.repository.ShopRepository
import com.LetterQuest.domain.repository.TokenRepository
import com.LetterQuest.domain.usecase.AdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShopEntry(
    val item: ShopItem,
    val isOwned: Boolean,
    val isAffordable: Boolean
)

/** Purchasable coin bundles shown in the shop. */
enum class CoinBundle(val label: String, val tokens: Int, val price: String, val icon: String, val isBestValue: Boolean = false) {
    STARTER("Starter Pack", 100, "$0.49", "⭐"),
    ECONOMY("Economy Pack", 500, "$1.99", "💰"),
    PREMIUM("Premium Pack", 5000, "$4.99", "👑", isBestValue = true)
}

/** In-App Purchase products */
enum class InAppProduct(val id: String, val title: String, val price: String, val description: String) {
    REMOVE_ADS("remove_ads", "Remove Ads", "$9.99", "Remove all ads and enjoy uninterrupted gameplay"),
}

private const val REWARDED_AD_TOKEN_REWARD = 50

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val tokenRepository: TokenRepository,
    private val dailyLoginRepository: DailyLoginRepository,
    private val preferencesRepository: PreferencesRepository,
    private val adManager: AdManager,
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _purchaseMessage = MutableStateFlow<String?>(null)
    val purchaseMessage: StateFlow<String?> = _purchaseMessage.asStateFlow()

    val tokenBalance: StateFlow<Int> = tokenRepository.observeTokens()
        .map { it.balance }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = UserTokens.STARTING_BALANCE
        )

    val dailyLoginState: StateFlow<DailyLoginReward> =
        dailyLoginRepository.observeDailyLoginState()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = DailyLoginReward()
            )

    val entries: StateFlow<List<ShopEntry>> = combine(
        shopRepository.observeOwnedItems(),
        tokenRepository.observeTokens()
    ) { owned, tokens ->
        ShopItem.entries.map { item ->
            ShopEntry(
                item = item,
                isOwned = item in owned,
                isAffordable = tokens.canAfford(item.cost)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    /**
     * Activates [item] for the current game. Power-ups are single-use per game.
     * The spend runs first so a failed debit cannot hand out a free perk.
     */
    fun purchase(item: ShopItem) {
        viewModelScope.launch {
            val owned = shopRepository.getOwnedItems().getOrNull().orEmpty()
            if (item in owned) {
                _purchaseMessage.value = "${item.displayName} is already active for this game"
                return@launch
            }

            tokenRepository.spendTokens(item.cost)
                .onSuccess {
                    shopRepository.markPurchased(item)
                        .onSuccess {
                            _purchaseMessage.value = "✅ ${item.displayName} activated for this game!"
                        }
                        .onFailure {
                            tokenRepository.earnTokens(item.cost)
                            _purchaseMessage.value = "Purchase failed — tokens refunded"
                        }
                }
                .onFailure {
                    _purchaseMessage.value = "Not enough tokens for ${item.displayName}"
                }
        }
    }

    /** Claims today's daily login reward if available. */
    fun claimDailyReward() {
        viewModelScope.launch {
            dailyLoginRepository.claimDailyReward()
                .onSuccess { tokens ->
                    _purchaseMessage.value = "🎁 Daily reward claimed! +$tokens 🪙"
                }
                .onFailure {
                    _purchaseMessage.value = "Already claimed today. Come back tomorrow!"
                }
        }
    }

    /**
     * Simulates purchasing a [CoinBundle]. In a real app this triggers an IAP flow;
     * here we award the tokens directly as a demo.
     */
    fun purchaseCoinBundle(bundle: CoinBundle) {
        viewModelScope.launch {
            tokenRepository.earnTokens(bundle.tokens)
            shopRepository.recordIAP()
            _purchaseMessage.value = "${bundle.icon} ${bundle.label}: +${bundle.tokens} 🪙 added!"
        }
    }

    /** Purchase Remove Ads in-app product */
    fun purchaseRemoveAds(activity: android.app.Activity? = null) {
        viewModelScope.launch {
            if (billingRepository.isPurchased(InAppPurchases.REMOVE_ADS.id)) {
                _purchaseMessage.value = "Ads are already removed!"
                return@launch
            }
            billingRepository.setActivity(activity)
            billingRepository.clearMessage()
            billingRepository.launchPurchase(InAppPurchases.REMOVE_ADS)
            val message = billingRepository.purchaseMessage.first { it != null }
            _purchaseMessage.value = message
            if (message!!.contains("successful", ignoreCase = true)) {
                adManager.setAdsRemoved(true)
                preferencesRepository.setAdsRemoved(true)
            }
        }
    }

    /** Called by rewarded-ad flows when the user earned a reward. */
    fun onRewardedAdWatched(amount: Int) {
        _purchaseMessage.value = "🎁 Reward earned! +$amount 🪙"
    }

    /**
     * Called by rewarded-ad flows for a power-up card. Grants the fixed 50-token
     * reward (test ads only report ~10 via the SDK, so we never use that amount)
     * AND activates the perk for the current game.
     */
    fun onPowerUpAdWatched(item: ShopItem) {
        viewModelScope.launch {
            tokenRepository.earnTokens(REWARDED_AD_TOKEN_REWARD)
            shopRepository.markPurchased(item)
                .onSuccess {
                    _purchaseMessage.value =
                        "✅ ${item.displayName} activated + 🪙 $REWARDED_AD_TOKEN_REWARD tokens!"
                }
                .onFailure {
                    // Already active this game — still pay out the tokens only.
                    _purchaseMessage.value =
                        "🎁 +🪙 $REWARDED_AD_TOKEN_REWARD tokens (${item.displayName} already active)"
                }
        }
    }

    /** Surfaces a non-blocking info/error message to the shop banner. */
    fun notifyPurchaseError(message: String) {
        _purchaseMessage.value = message
    }

    fun clearPurchaseMessage() {
        _purchaseMessage.value = null
    }
}
