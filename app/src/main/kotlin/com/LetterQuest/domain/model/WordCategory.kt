package com.LetterQuest.domain.model

/**
 * A themed group of words the player can choose to play from.
 *
 * [id] is the stable key stored in preferences and matched against [Word.category];
 * [name] and [icon] are display-only.
 */
data class WordCategory(
    val id: String,
    val name: String,
    val icon: String
) {
    init {
        require(id.isNotBlank()) { "Category id cannot be blank" }
        require(name.isNotBlank()) { "Category name cannot be blank" }
    }

    companion object {
        /** Sentinel used by the picker to mean "draw from every category". */
        const val ALL_CATEGORIES_ID = "all"
    }
}
