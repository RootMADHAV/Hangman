package com.LetterQuest.domain.repository

import com.LetterQuest.domain.model.InAppPurchase
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val isBillingReady: StateFlow<Boolean>
    val purchaseMessage: StateFlow<String?>
    fun setActivity(activity: android.app.Activity?)
    fun launchPurchase(product: InAppPurchase)
    fun isPurchased(productId: String): Boolean
    fun queryPurchases()
    fun clearMessage()
}
