@file:OptIn(ExperimentalMaterial3Api::class)

package com.LetterQuest.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.LetterQuest.domain.model.WordCategory
import com.LetterQuest.ui.navigation.NavigationRoute
import com.LetterQuest.ui.viewmodel.CategorySelectViewModel

@Composable
fun CategorySelectScreen(
    navController: NavHostController,
    difficulty: String,
    viewModel: CategorySelectViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    fun startGame(categoryId: String) {
        navController.navigate(
            NavigationRoute.Gameplay.routeWithCategory(difficulty, categoryId)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose a Category") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.semantics { contentDescription = "Navigate back" }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
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
                when {
                    uiState.isLoading -> {
                        Text("Loading categories...", fontSize = 16.sp)
                    }

                    uiState.error != null -> {
                        Text(
                            "Could not load categories: ${uiState.error}",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    else -> {
                        CategoryCard(
                            icon = "🎲",
                            name = "Surprise Me",
                            subtitle = "Words from every category",
                            onClick = { startGame(WordCategory.ALL_CATEGORIES_ID) }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            "Categories",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        uiState.categories.forEachIndexed { index, category ->
                            AnimatedVisibility(
                                visible = true,
                                enter = scaleIn(animationSpec = tween(200 + index * 30)) +
                                    fadeIn(animationSpec = tween(200 + index * 30))
                            ) {
                                CategoryCard(
                                    icon = category.icon,
                                    name = category.name,
                                    subtitle = null,
                                    onClick = { startGame(category.id) }
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    icon: String,
    name: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(if (subtitle == null) 64.dp else 76.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(icon, fontSize = 28.sp, modifier = Modifier.padding(end = 16.dp))
            Column {
                Text(name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
