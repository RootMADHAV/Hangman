package com.LetterQuest.ui.viewmodel

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.LetterQuest.domain.usecase.AdManager
import android.app.Activity

class AdViewModelTest {

    private lateinit var viewModel: AdViewModel

    @Before
    fun setup() {
        viewModel = AdViewModel(AdManager())
    }

    @Test
    fun testInitialAdState() {
        val state = viewModel.adState.value
        assertTrue(state.showBannerAd)
        assertFalse(state.showInterstitialAd)
    }

    @Test
    fun testShowInterstitialAd() = runTest {
        val activity = object : Activity() {}
        val shown = viewModel.showInterstitialAd(activity)
        assertFalse("showInterstitialAd should return false when no ad is cached", shown)
        assertFalse(viewModel.adState.value.showInterstitialAd)
    }

    @Test
    fun testDismissInterstitialAd() = runTest {
        val activity = object : Activity() {}
        viewModel.showInterstitialAd(activity)
        viewModel.dismissInterstitialAd()
        val state = viewModel.adState.value
        assertFalse(state.showInterstitialAd)
    }

    @Test
    fun testToggleBannerAd() = runTest {
        viewModel.toggleBannerAd(false)
        assertFalse(viewModel.adState.value.showBannerAd)

        viewModel.toggleBannerAd(true)
        assertTrue(viewModel.adState.value.showBannerAd)
    }

    @Test
    fun testSetAdError() = runTest {
        val errorMsg = "Ad load failed"
        viewModel.setAdError(errorMsg)
        val state = viewModel.adState.value
        assert(state.adLoadingError == errorMsg)
    }
}
