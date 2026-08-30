@file:OptIn(ExperimentalMaterial3Api::class)

package com.LetterQuest.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.LetterQuest.domain.model.DailyLoginReward
import com.LetterQuest.domain.model.RewardType
import com.LetterQuest.ui.components.BannerAd
import com.LetterQuest.ui.components.RemoveAdsPurchaseCard
import com.LetterQuest.ui.components.RewardedAdCard
import com.LetterQuest.ui.viewmodel.AdViewModel
import com.LetterQuest.ui.viewmodel.CoinBundle
import com.LetterQuest.ui.viewmodel.RewardedAdViewModel
import com.LetterQuest.ui.viewmodel.ShopEntry
import com.LetterQuest.ui.viewmodel.ShopViewModel
import com.LetterQuest.util.findActivity

@Composable
fun ShopScreen(
    navController: NavHostController,
    viewModel: ShopViewModel = hiltViewModel(),
    adViewModel: AdViewModel = hiltViewModel(),
    rewardedAdViewModel: RewardedAdViewModel = hiltViewModel()
) {
    val entries = viewModel.entries.collectAsState().value
    val tokenBalance = viewModel.tokenBalance.collectAsState().value
    val message = viewModel.purchaseMessage.collectAsState().value
    val dailyLogin = viewModel.dailyLoginState.collectAsState().value
    val adsRemoved by rewardedAdViewModel.adsRemoved.collectAsState()
    val adState = adViewModel.adState.collectAsState().value

    val context = LocalContext.current
    LaunchedEffect(adsRemoved) {
        if (!adsRemoved) rewardedAdViewModel.loadRewardedAd(context, RewardType.COINS)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🛒 Shop", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.semantics { contentDescription = "Navigate back" }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Text(
                        "🪙 $tokenBalance",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        bottomBar = { BannerAd(modifier = Modifier.fillMaxWidth(), showAds = adState.showBannerAd) }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                // ── Message banner ────────────────────────────────────────────
                if (message != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            message,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(14.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Daily Login Reward ────────────────────────────────────────
                SectionHeader("🎁 Daily Login Reward")
                Spacer(modifier = Modifier.height(8.dp))
                DailyLoginCard(
                    canClaim = dailyLogin.canClaim,
                    streak = dailyLogin.currentStreak,
                    reward = dailyLogin.tokensReward,
                    onClaim = { viewModel.claimDailyReward() }
                )
                Spacer(modifier = Modifier.height(24.dp))

                // ── Power-ups ─────────────────────────────────────────────────
                SectionHeader("⚡ Game Power-ups")
                Text(
                    "Activate before or during a game. Single-use per level.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                entries.forEach { entry ->
                    ShopItemCard(
                        entry = entry,
                        onBuy = { viewModel.purchase(entry.item) },
                        onWatchAd = if (adsRemoved) null else ({
                            val activity = context.findActivity()
                            val shown = activity != null && rewardedAdViewModel.showRewardedAd(
                                RewardType.COINS, activity, context,
                                onReward = {
                                    // Fixed 50-token reward + activate this power-up.
                                    viewModel.onPowerUpAdWatched(entry.item)
                                }
                            )
                            if (!shown) viewModel.notifyPurchaseError(
                                "⏳ Ad is loading — please try again in a moment"
                            )
                        })
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                // ── Coin Bundles ──────────────────────────────────────────────
                SectionHeader("💰 Buy Tokens")
                Text(
                    "Instantly boost your wallet with a token pack.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                CoinBundle.entries.forEach { bundle ->
                    CoinBundleCard(
                        bundle = bundle,
                        onBuy = { viewModel.purchaseCoinBundle(bundle) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Free tokens via rewarded ad
                if (!adsRemoved) {
                    RewardedAdCard(
                        rewardText = "+50 🪙",
                        description = "Free tokens — no purchase needed",
                        icon = "🎁",
                        onWatchClick = {
                            val activity = context.findActivity()
                            val shown = activity != null && rewardedAdViewModel.showRewardedAd(
                                RewardType.COINS, activity, context,
                                onReward = {
                                    // Fixed 50-token reward (test ads report ~10 via the SDK).
                                    rewardedAdViewModel.grantTokens(50)
                                    viewModel.onRewardedAdWatched(50)
                                }
                            )
                            if (!shown) viewModel.notifyPurchaseError(
                                "⏳ Ad is loading — please try again in a moment"
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // ── Remove Ads IAP ──────────────────────────────────────────────
                RemoveAdsPurchaseCard(
                    alreadyOwned = adsRemoved,
                    onBuyClick = {
                        val activity = context.findActivity()
                        viewModel.purchaseRemoveAds(activity)
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun DailyLoginCard(
    canClaim: Boolean,
    streak: Int,
    reward: Int,
    onClaim: () -> Unit
) {
    val rewards = DailyLoginReward.REWARDS
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (canClaim) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎁", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (canClaim) "Claim your daily reward!" else "Come back tomorrow!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            // 7-day streak calendar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rewards.forEachIndexed { index, dayReward ->
                    val dayNumber = index + 1
                    val isDone = streak >= dayNumber
                    val isToday = streak + 1 == dayNumber && canClaim
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp),
                            color = when {
                                isDone -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.surface
                            },
                            border = if (isToday) BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary) else null
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    if (isDone) "✓" else "$dayNumber",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isDone -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.onTertiary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "+$dayReward",
                            fontSize = 9.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Today's reward: 🪙 $reward tokens",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(
                onClick = onClaim,
                enabled = canClaim,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text(if (canClaim) "✅ Claim Now" else "✓ Claimed")
            }
        }
    }
}

@Composable
private fun CoinBundleCard(bundle: CoinBundle, onBuy: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (bundle.isBestValue) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        border = if (bundle.isBestValue) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (bundle.isBestValue) 6.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(bundle.icon, fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(bundle.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    if (bundle.isBestValue) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                "BEST VALUE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    "🪙 ${bundle.tokens} tokens",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(bundle.price, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ShopItemCard(
    entry: ShopEntry,
    onBuy: () -> Unit,
    onWatchAd: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (entry.isOwned) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                entry.item.icon,
                fontSize = 30.sp,
                modifier = Modifier.padding(end = 14.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.item.displayName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    entry.item.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (entry.isOwned) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        "✓ Active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.End) {
                    Button(
                        onClick = onBuy,
                        enabled = entry.isAffordable
                    ) {
                        Text("🪙 ${entry.item.cost}", fontSize = 13.sp)
                    }
                    if (onWatchAd != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(onClick = onWatchAd) {
                            Text("📺 Free (Ad)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
