package com.LetterQuest.domain.model

object AdConfig {
    // Official Google AdMob test IDs (https://developers.google.com/admob/android/test-ads)
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"

    // Banner ads on 7 screens — official banner test ad unit
    private const val BANNER_TEST = "ca-app-pub-3940256099942544/6300978111"
    const val BANNER_HOME = BANNER_TEST
    const val BANNER_GAMEPLAY = BANNER_TEST
    const val BANNER_RESULT = BANNER_TEST
    const val BANNER_STATS = BANNER_TEST
    const val BANNER_ACHIEVEMENTS = BANNER_TEST
    const val BANNER_PROGRESS = BANNER_TEST
    const val BANNER_SHOP = BANNER_TEST

    // Interstitial: every 10 completed games — official interstitial test ad unit
    const val INTERSTITIAL_GAME_COMPLETE = "ca-app-pub-3940256099942544/1033173712"

    // Rewarded: hints — official rewarded test ad unit
    const val REWARDED_HINTS = "ca-app-pub-3940256099942544/5224354917"

    // Rewarded: watch ad earn coins — official rewarded test ad unit
    const val REWARDED_COINS = "ca-app-pub-3940256099942544/5224354917"
}

enum class RewardType {
    HINT, COINS
}

data class InAppPurchase(
    val id: String,
    val name: String,
    val description: String,
    val price: String,
    val priceAmountMicros: Long
)

object InAppPurchases {
    val REMOVE_ADS = InAppPurchase(
        id = "remove_ads",
        name = "Remove Ads",
        description = "Remove all ads from the game",
        price = "$2.99",
        priceAmountMicros = 9990000
    )
}
