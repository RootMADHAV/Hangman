package com.hangman.domain.model

data class TokenPack(
    val id: String,
    val tokens: Int,
    val priceUSD: Double,
    val displayName: String,
    val description: String,
    val icon: String,
    val isBestValue: Boolean = false
) {
    companion object {
        val STARTER_PACK = TokenPack(
            id = "tokens_starter",
            tokens = 100,
            priceUSD = 0.49,
            displayName = "Starter Pack",
            description = "100 tokens",
            icon = "⭐",
            isBestValue = false
        )

        val ECONOMY_PACK = TokenPack(
            id = "tokens_economy",
            tokens = 500,
            priceUSD = 1.99,
            displayName = "Economy Pack",
            description = "500 tokens",
            icon = "💰",
            isBestValue = false
        )

        val PREMIUM_PACK = TokenPack(
            id = "tokens_premium",
            tokens = 5000,
            priceUSD = 4.99,
            displayName = "Premium Pack",
            description = "5000 tokens",
            icon = "👑",
            isBestValue = true
        )

        val allPacks = listOf(STARTER_PACK, ECONOMY_PACK, PREMIUM_PACK)
    }
}

