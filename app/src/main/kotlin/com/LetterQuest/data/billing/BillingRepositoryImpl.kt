package com.LetterQuest.data.billing

import com.LetterQuest.domain.model.InAppPurchase
import com.LetterQuest.domain.repository.BillingRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val billingManager: BillingManager
) : BillingRepository {

    override val isBillingReady: StateFlow<Boolean>
        get() = billingManager.isBillingReady

    override val purchaseMessage: StateFlow<String?>
        get() = billingManager.purchaseMessage

    override fun setActivity(activity: android.app.Activity?) {
        billingManager.setActivity(activity)
    }

    override fun launchPurchase(product: InAppPurchase) {
        billingManager.launchBillingFlow(product = product)
    }

    override fun isPurchased(productId: String): Boolean {
        return billingManager.isPurchased(productId)
    }

    override fun queryPurchases() {
        billingManager.queryPurchases()
    }

    override fun clearMessage() {
        billingManager.clearMessage()
    }
}
