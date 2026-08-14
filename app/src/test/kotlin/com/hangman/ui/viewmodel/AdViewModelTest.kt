package com.hangman.ui.viewmodel

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdViewModelTest {

    private lateinit var viewModel: AdViewModel

    @Before
    fun setup() {
        viewModel = AdViewModel()
    }

    @Test
    fun testInitialAdState() {
        val state = viewModel.adState.value
        assertTrue(state.showBannerAd)
        assertFalse(state.showInterstitialAd)
    }

    @Test
    fun testShowInterstitialAd() = runTest {
        viewModel.showInterstitialAd()
        val state = viewModel.adState.value
        assertTrue(state.showInterstitialAd)
    }

    @Test
    fun testDismissInterstitialAd() = runTest {
        viewModel.showInterstitialAd()
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
