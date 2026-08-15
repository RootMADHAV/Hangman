package com.LetterQuest.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.LetterQuest.domain.usecase.AdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

import android.app.Activity

data class AdUIState(
    val showBannerAd: Boolean = true,
    val showInterstitialAd: Boolean = false,
    val adLoadingError: String? = null
)

@HiltViewModel
class AdViewModel @Inject constructor(
    private val adManager: AdManager
) : ViewModel() {

    private val _adState = MutableStateFlow(AdUIState())
    val adState: StateFlow<AdUIState> = _adState.asStateFlow()

    fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit = {}): Boolean {
        val shown = adManager.showInterstitialAd(activity) {
            _adState.value = _adState.value.copy(showInterstitialAd = false)
            onDismissed()
        }
        if (shown) {
            _adState.value = _adState.value.copy(showInterstitialAd = true)
        }
        return shown
    }

    fun dismissInterstitialAd() {
        _adState.value = _adState.value.copy(showInterstitialAd = false)
    }

    fun toggleBannerAd(show: Boolean) {
        _adState.value = _adState.value.copy(showBannerAd = show)
    }

    fun setAdError(error: String?) {
        _adState.value = _adState.value.copy(adLoadingError = error)
    }
}
