package com.LetterQuest.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LetterQuest.domain.model.InAppPurchases

/**
 * Material3 card for the "Remove Ads" in-app purchase. [onBuyClick] should
 * trigger the IAP flow and, on success, `AdManager.setAdsRemoved(true)`.
 */
@Composable
fun RemoveAdsPurchaseCard(
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier,
    price: String = InAppPurchases.REMOVE_ADS.price,
    alreadyOwned: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🚫", fontSize = 30.sp, modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Remove Ads", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (alreadyOwned) "Ads are removed — enjoy!"
                    else "Remove all ads forever",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (alreadyOwned) {
                Text(
                    "✓ Owned",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            } else {
                Button(onClick = onBuyClick) {
                    Text(price, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
