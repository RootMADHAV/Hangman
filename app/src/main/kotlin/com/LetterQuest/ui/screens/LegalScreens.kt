@file:OptIn(ExperimentalMaterial3Api::class)

package com.LetterQuest.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun PrivacyPolicyScreen(navController: NavHostController) {
    LegalScaffold(title = "Privacy Policy", onBack = { navController.popBackStack() }) {
        Text(
            text = """
                Privacy Policy

                Last updated: August 2026

                1. Information We Collect
                Letter Quest stores your game progress, preferences, statistics, and achievements locally on your device. We do not collect or transmit personal information to our own servers.

                2. Advertising
                The app may display ads provided by Google AdMob. AdMob may collect device identifiers and usage data in accordance with Google's privacy policy. You can manage ad personalization in your Google account settings.

                3. Data Storage
                All game data is stored locally using on-device storage and is never uploaded externally by the app itself.

                4. Third-Party Services
                Google AdMob: https://policies.google.com/privacy

                5. Changes
                We may update this policy from time to time. Continued use of the app after changes constitutes acceptance of the updated policy.

                6. Contact
                For questions about this policy, contact the developer via the Google Play store listing.
            """.trimIndent(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun TermsScreen(navController: NavHostController) {
    LegalScaffold(title = "Terms of Service", onBack = { navController.popBackStack() }) {
        Text(
            text = """
                Terms of Service

                Last updated: August 2026

                1. Acceptance
                 By using Letter Quest, you agree to these terms. If you do not agree, please uninstall the app.

                2. License
                We grant you a personal, non-exclusive, non-transferable license to use the app for entertainment purposes only.

                3. In-App Content
                All words, categories, achievements, and visuals are provided as-is for gameplay entertainment.

                4. Purchases
                The app may offer optional in-app purchases of virtual items (tokens, themes). Virtual items have no real-world value and are non-refundable except as required by law or Google Play policy.

                5. Advertisements
                The app displays third-party ads. We are not responsible for the content of third-party advertisements.

                6. Disclaimer
                The app is provided "as is" without warranties of any kind. We are not liable for any damages arising from use of the app.

                7. Termination
                We reserve the right to modify or discontinue the app at any time without notice.
            """.trimIndent(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun LegalScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Navigate back" }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            content()
        }
    }
}
