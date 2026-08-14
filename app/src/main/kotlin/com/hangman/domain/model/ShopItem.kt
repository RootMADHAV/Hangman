package com.hangman.domain.model

/**
 * A permanent perk bought with earned tokens.
 *
 * These are deliberately not real-money purchases — tokens come from play, so the shop
 * is a way to spend a surplus rather than a monetisation surface.
 */
enum class ShopItem(
    val id: String,
    val cost: Int,
    val displayName: String,
    val description: String,
    val icon: String
) {
    HINT_DISCOUNT(
        id = "hint_discount",
        cost = 90,
        displayName = "Bargain Hunter",
        description = "All hints cost 25% less for this game only",
        icon = "🏷️"
    ),
    SCORE_BOOST(
        id = "score_boost",
        cost = 120,
        displayName = "Double Down",
        description = "Earn 50% more score for this game only",
        icon = "📈"
    );

    companion object {
        fun fromId(id: String): ShopItem? = entries.find { it.id == id }

        /** Multiplier applied to hint costs once [HINT_DISCOUNT] is owned. */
        const val HINT_DISCOUNT_MULTIPLIER = 0.75f

        /** Multiplier applied to won-game score once [SCORE_BOOST] is owned. */
        const val SCORE_BOOST_MULTIPLIER = 1.5f
    }
}
