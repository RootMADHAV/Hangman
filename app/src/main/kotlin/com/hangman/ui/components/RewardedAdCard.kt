package com.hangman.ui.components

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

/**
 * Material3 card offering the player a rewarded ad in exchange for [rewardText]
 * (e.g. "+50 🪙" or "20 tokens"). The caller wires [onWatchClick] to
 * `AdManager.getRewardedAd(...)` + `RewardedAd.show(activity, ...)`.
 *
 * @param enabled should be `false` while no rewarded ad is loaded yet.
 */
@Composable
fun RewardedAdCard(
    rewardText: String,
    onWatchClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Watch Ad",
    description: String = "Watch a short video to earn your reward",
    icon: String = "📺",
    enabled: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 30.sp, modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Watch Ad to get $rewardText",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(onClick = onWatchClick, enabled = enabled) {
                Text(
                    if (enabled) "▶ Watch" else "Loading…",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
