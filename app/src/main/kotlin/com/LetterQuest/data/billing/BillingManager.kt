package com.LetterQuest.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.LetterQuest.domain.model.InAppPurchase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener, BillingClientStateListener {

    private val _purchases = MutableStateFlow<Map<String, Purchase>>(emptyMap())
    val purchases: StateFlow<Map<String, Purchase>> = _purchases.asStateFlow()

    private val _isBillingReady = MutableStateFlow(false)
    val isBillingReady: StateFlow<Boolean> = _isBillingReady.asStateFlow()

    private val _purchaseMessage = MutableStateFlow<String?>(null)
    val purchaseMessage: StateFlow<String?> = _purchaseMessage.asStateFlow()

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private var currentActivity: Activity? = null

    init {
        startConnection()
    }

    fun setActivity(activity: Activity?) {
        currentActivity = activity
    }

    private fun startConnection() {
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _isBillingReady.value = true
            queryPurchases()
        } else {
            Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
        }
    }

    override fun onBillingServiceDisconnected() {
        _isBillingReady.value = false
        Log.w(TAG, "Billing service disconnected")
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseMessage.value = "Purchase canceled"
            }
            else -> {
                _purchaseMessage.value = "Purchase failed: ${billingResult.debugMessage}"
                Log.e(TAG, "Purchase error: ${billingResult.debugMessage}")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
            val updated = _purchases.value.toMutableMap()
            val key = (purchase.products.firstOrNull() ?: purchase.orderId) as String
            updated[key] = purchase
            _purchases.value = updated
            _purchaseMessage.value = "✅ Purchase successful!"
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged")
            }
        }
    }

    fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val map = purchases.associateBy({ (it.products.firstOrNull() ?: it.orderId) as String })
                _purchases.value = map
            }
        }
    }

    fun launchBillingFlow(activity: Activity? = null, product: InAppPurchase) {
        if (!_isBillingReady.value) {
            _purchaseMessage.value = "Billing not ready yet"
            return
        }

        val targetActivity = activity ?: currentActivity
        if (targetActivity == null) {
            _purchaseMessage.value = "No activity available for purchase"
            return
        }

        val params = SkuDetailsParams.newBuilder()
            .setSkusList(listOf(product.id))
            .setType(BillingClient.SkuType.INAPP)
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                val skuDetails = skuDetailsList.firstOrNull()
                if (skuDetails != null) {
                    val flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build()
                    billingClient.launchBillingFlow(targetActivity, flowParams)
                } else {
                    _purchaseMessage.value = "Product not found"
                }
            } else {
                _purchaseMessage.value = "Failed to load product: ${billingResult.debugMessage}"
            }
        }
    }

    fun isPurchased(productId: String): Boolean {
        return _purchases.value[productId]?.purchaseState == Purchase.PurchaseState.PURCHASED
    }

    fun clearMessage() {
        _purchaseMessage.value = null
    }

    fun endConnection() {
        billingClient.endConnection()
    }

    companion object {
        private const val TAG = "BillingManager"
    }
}
