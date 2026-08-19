@file:OptIn(ExperimentalMaterial3Api::class)

package com.LetterQuest.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.LetterQuest.domain.model.AvatarCatalog
import com.LetterQuest.domain.model.AvatarOption
import com.LetterQuest.domain.model.PlayerLevel
import com.LetterQuest.domain.model.UsernameValidator
import com.LetterQuest.ui.navigation.NavigationRoute
import com.LetterQuest.ui.viewmodel.AuthViewModel
import com.LetterQuest.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val profileState = profileViewModel.profileState.collectAsState().value
    val authState by authViewModel.uiState.collectAsState()
    var showLinkDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editNickname by remember { mutableStateOf("") }
    var editUsername by remember { mutableStateOf("") }
    var editAvatarId by remember { mutableIntStateOf(0) }
    var usernameError by remember { mutableStateOf<String?>(null) }

    val currentUser = authState.currentUser
    val avatarOption = AvatarCatalog.getAvatarById(profileState.avatarId) ?: AvatarCatalog.getDefaultAvatar()
    val accountType = when {
        currentUser == null -> "Unknown"
        currentUser.isGuest -> "Guest"
        else -> "Email"
    }

    if (showEditDialog) {
        LaunchedEffect(showEditDialog) {
            editNickname = profileState.nickname
            editUsername = profileState.username
            editAvatarId = AvatarCatalog.avatars.indexOfFirst { it.id == profileState.avatarId }.coerceAtLeast(0)
            usernameError = null
        }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = editNickname,
                        onValueChange = { editNickname = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = {
                            editUsername = it
                            usernameError = null
                        },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = usernameError != null,
                        supportingText = {
                            if (usernameError != null) {
                                Text(text = usernameError!!, color = MaterialTheme.colorScheme.error)
                            } else if (editUsername.isNotBlank()) {
                                Text(
                                    text = "3-20 chars, letters, numbers, _, -",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Choose Avatar", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    AvatarGrid(
                        selectedIndex = editAvatarId,
                        onSelect = { editAvatarId = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val usernameValidation = UsernameValidator.validate(editUsername)
                        if (usernameValidation is com.LetterQuest.domain.model.ValidationResult.Error) {
                            usernameError = usernameValidation.message
                            return@TextButton
                        }
                        val validatedUsername = (usernameValidation as com.LetterQuest.domain.model.ValidationResult.Success).value
                        if (editNickname.isNotBlank()) {
                            authViewModel.updateNickname(editNickname.trim())
                        }
                        authViewModel.updateUsername(validatedUsername)
                        authViewModel.updateAvatar(AvatarCatalog.avatars[editAvatarId].id)
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentUser != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(40.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = avatarOption.emoji,
                                fontSize = 40.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = profileState.nickname.ifBlank { currentUser.displayName ?: "Player" },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (profileState.username.isNotBlank()) {
                            Text(
                                text = "@${profileState.username}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = accountType,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        val playerLevel = PlayerLevel.from(profileState.statistics.gamesWon, profileState.statistics.totalScore)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    "Lv ${playerLevel.level}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.padding(start = 8.dp))
                            Text(
                                playerLevel.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { playerLevel.progress },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )
                        Text(
                            "${playerLevel.currentXp} / ${playerLevel.xpForNextLevel} XP",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        if (currentUser.email != null) {
                            Text(
                                text = currentUser.email,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        if (currentUser.isGuest) {
                            Text(
                                text = "Guest Account",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        if (currentUser.isEmailVerified) {
                            Text(
                                text = "✓ Email Verified",
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Profile")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentUser.isGuest) {
                        OutlinedButton(
                            onClick = { showLinkDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null)
                            Spacer(modifier = Modifier.padding(start = 4.dp))
                            Text("Link Email")
                        }
                    } else if (!currentUser.isEmailVerified) {
                        OutlinedButton(
                            onClick = { authViewModel.sendEmailVerification() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null)
                            Spacer(modifier = Modifier.padding(start = 4.dp))
                            Text("Verify Email")
                        }
                    }
                    OutlinedButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.padding(start = 4.dp))
                        Text("Sign Out")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Not signed in", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { navController.navigate(NavigationRoute.Auth.route) }
                        ) {
                            Text("Sign In / Create Account")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "Player Statistics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            StatCard(
                title = "Total Games",
                value = profileState.totalGames.toString(),
                backgroundColor = Color(0xFFE3F2FD)
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatCard(
                title = "Games Won",
                value = profileState.statistics.gamesWon.toString(),
                backgroundColor = Color(0xFFC8E6C9)
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatCard(
                title = "Win Percentage",
                value = String.format("%.1f%%", profileState.winPercentage),
                backgroundColor = Color(0xFFFFF9C4)
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatCard(
                title = "Total Score",
                value = profileState.statistics.totalScore.toString(),
                backgroundColor = Color(0xFFFFE0B2)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Achievements",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            if (profileState.achievements.isEmpty()) {
                Text(
                    text = "No achievements yet",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                profileState.achievements.forEach { achievement ->
                    AchievementCard(achievement)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showLinkDialog) {
        LinkEmailDialog(
            onDismiss = { showLinkDialog = false },
            onLink = { email, password ->
                authViewModel.linkGuestToEmail(email, password)
                showLinkDialog = false
            }
        )
    }

    if (showSignOutDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out? Your local progress will be preserved.") },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.signOut()
                    showSignOutDialog = false
                    navController.navigate(NavigationRoute.Auth.route) {
                        popUpTo(NavigationRoute.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }) {
                    Text("Sign Out", color = Color(0xFFB71C1C))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AvatarGrid(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val rows = AvatarCatalog.avatars.chunked(5)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowAvatars ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowAvatars.forEach { avatar ->
                    val index = AvatarCatalog.avatars.indexOf(avatar)
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = avatar.emoji, fontSize = 28.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkEmailDialog(
    onDismiss: () -> Unit,
    onLink: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Link Guest to Email") },
        text = {
            Column {
                Text("Create an account to permanently save your progress.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onLink(email, password) },
                enabled = email.isNotBlank() && password.length >= 6
            ) {
                Text("Link Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    backgroundColor: Color
) {
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()

    val actualBackgroundColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        backgroundColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = actualBackgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AchievementCard(achievement: com.LetterQuest.domain.model.Achievement) {
    val statusText = if (achievement.unlockedAt != null) "✓ Unlocked" else "Locked"
    val statusColor = if (achievement.unlockedAt != null) Color.Green else Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = achievement.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = achievement.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
