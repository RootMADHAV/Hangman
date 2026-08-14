@file:OptIn(ExperimentalMaterial3Api::class)

package com.hangman.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.hangman.domain.model.ThemePreset
import com.hangman.ui.viewmodel.ThemeCustomizationViewModel
import com.hangman.ui.viewmodel.ThemePresetEntry

@Composable
fun ThemeCustomizationScreen(
    navController: NavHostController,
    viewModel: ThemeCustomizationViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Themes", fontWeight = FontWeight.Bold)
                        Text("🪙 ${uiState.tokenBalance}", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (uiState.message != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            uiState.message,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                val free = uiState.presets.filter { it.preset.cost == 0 }
                val premium = uiState.presets.filter { it.preset.cost > 0 }

                SectionLabel("Free Themes")
                Spacer(modifier = Modifier.height(8.dp))
                free.forEach { entry ->
                    ThemeCard(
                        entry = entry,
                        isSelected = entry.preset.id == uiState.selectedPresetId,
                        onSelect = { viewModel.selectPreset(entry.preset.id) },
                        onPurchase = {}
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                SectionLabel("Premium Themes")
                Text(
                    "Unlock once — yours forever.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                premium.forEach { entry ->
                    ThemeCard(
                        entry = entry,
                        isSelected = entry.preset.id == uiState.selectedPresetId,
                        onSelect = { if (entry.isUnlocked) viewModel.selectPreset(entry.preset.id) },
                        onPurchase = { viewModel.purchaseAndApply(entry.preset) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
private fun ThemeCard(
    entry: ThemePresetEntry,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPurchase: () -> Unit
) {
    val preset = entry.preset
    val colors = preset.colors

    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = entry.isUnlocked) { onSelect() }
            .alpha(if (!entry.isUnlocked && !entry.isAffordable) 0.7f else 1f),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = if (isSelected) 6.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color swatches preview
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ColorDot(Color(colors.primary))
                ColorDot(Color(colors.secondary))
                ColorDot(Color(colors.background))
                ColorDot(Color(colors.tertiary))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(preset.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    if (preset.isDark) "Dark" else "Light",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when {
                isSelected && entry.isUnlocked -> {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                        Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(6.dp))
                    }
                }
                entry.isUnlocked -> {
                    Text("Tap to apply", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {
                    Button(
                        onClick = onPurchase,
                        enabled = entry.isAffordable,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        if (!entry.isAffordable) {
                            Icon(Icons.Default.Lock, null,
                                modifier = Modifier.size(14.dp).padding(end = 2.dp))
                        }
                        Text("🪙 ${preset.cost}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(color, CircleShape)
    )
}
