@file:OptIn(ExperimentalMaterial3Api::class)

package com.LetterQuest.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.LetterQuest.domain.model.AuthState
import com.LetterQuest.ui.navigation.NavigationRoute
import com.LetterQuest.ui.viewmodel.AuthViewModel
import com.LetterQuest.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@Composable
fun AuthScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            account.idToken?.let { idToken ->
                if (uiState.isGoogleLinkMode) {
                    authViewModel.linkGuestToGoogle(idToken)
                } else {
                    authViewModel.signInWithGoogle(idToken)
                }
            }
        } catch (e: ApiException) {
            authViewModel.clearError()
        }
    }

    val tutorialSettings by settingsViewModel.tutorialSettings.collectAsState()

    LaunchedEffect(uiState.authState) {
        if (uiState.authState is AuthState.Authenticated) {
            val showTutorial = tutorialSettings.showGameplayTutorial
            if (showTutorial) {
                settingsViewModel.markTutorialSeen("gameplay")
            }
            val destination = if (showTutorial) {
                NavigationRoute.Tutorial.route
            } else {
                NavigationRoute.Home.route
            }
            navController.navigate(destination) {
                popUpTo(NavigationRoute.Auth.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome") },
                navigationIcon = {
                    if (uiState.authState !is AuthState.Authenticated) {
                        IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.semantics { contentDescription = "Navigate back" }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("🎮", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Welcome to Letter Quest",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Sign in to save your progress",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFFB71C1C),
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.isForgotPasswordMode) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { authViewModel.sendPasswordReset(email) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading && email.isNotBlank()
                ) {
                    Text("Send Reset Link", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                if (uiState.passwordResetSent) {
                    Text(
                        "Password reset email sent. Check your inbox.",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(top = 8.dp),
                        fontSize = 13.sp
                    )
                }
                TextButton(onClick = { authViewModel.setForgotPasswordMode(false) }) {
                    Text("Back to Sign In")
                }
            } else {
                Button(
                    onClick = { authViewModel.signInAsGuest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading && uiState.authState !is AuthState.Authenticated
                ) {
                    Text("🎭 Continue as Guest", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(com.LetterQuest.R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading && uiState.authState !is AuthState.Authenticated
                ) {
                    Text("🔑 Continue with Google", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (uiState.isSignUpMode) "Create Account" else "Sign In with Email",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (uiState.isSignUpMode) {
                            authViewModel.signUpWithEmail(email, password)
                        } else {
                            authViewModel.signInWithEmail(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
                ) {
                    Text(
                        text = if (uiState.isSignUpMode) "Sign Up" else "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = { authViewModel.setSignUpMode(!uiState.isSignUpMode) },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = if (uiState.isSignUpMode) "Already have an account? Sign In" else "Need an account? Sign Up"
                    )
                }

                if (!uiState.isSignUpMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { authViewModel.setForgotPasswordMode(true) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Forgot Password?")
                    }
                }

                if (!uiState.isSignUpMode && uiState.currentUser?.isGuest == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { authViewModel.setEmailLinkMode(true) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null)
                        Spacer(modifier = Modifier.padding(start = 4.dp))
                        Text("Link guest progress to email")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { authViewModel.setGoogleLinkMode(true) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null)
                        Spacer(modifier = Modifier.padding(start = 4.dp))
                        Text("Link guest progress to Google")
                    }
                }
            }

            if (uiState.isEmailLinkMode && !uiState.isForgotPasswordMode) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Link Guest Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { authViewModel.linkGuestToEmail(email, password) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading && email.isNotBlank() && password.length >= 6
                        ) {
                            Text("Link Account")
                        }
                        TextButton(onClick = { authViewModel.setEmailLinkMode(false) }) {
                            Text("Cancel")
                        }
                    }
                }
            }

            if (uiState.isGoogleLinkMode && !uiState.isForgotPasswordMode) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Link Guest to Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Link your guest progress to a Google account. Your data will be preserved.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(context.getString(com.LetterQuest.R.string.default_web_client_id))
                                    .requestEmail()
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        ) {
                            Text("Link Google Account")
                        }
                        TextButton(onClick = { authViewModel.setGoogleLinkMode(false) }) {
                            Text("Cancel")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
