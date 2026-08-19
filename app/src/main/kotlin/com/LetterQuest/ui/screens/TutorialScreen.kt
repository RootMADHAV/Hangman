package com.LetterQuest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.LetterQuest.ui.navigation.NavigationRoute

private data class TutorialPage(val emoji: String, val title: String, val body: String)

private val PAGES = listOf(
    TutorialPage("🎮", "Welcome to Letter Quest!", "Guess the hidden word one letter at a time before the hangman is complete."),
    TutorialPage("🔤", "Guess Letters", "Tap any letter on the keyboard. Green means correct — it appears in the word. Red means wrong."),
    TutorialPage("💡", "Use Hints", "Stuck? Spend tokens on hints:\n• 💡 Show clue\n• 🔤 Reveal a letter\n• 🚫 Remove wrong letters\n• ⏭ Skip the word"),
    TutorialPage("🪙", "Earn Tokens", "Win a word → earn 3 tokens. Visit the Shop to buy more or grab your daily reward!"),
    TutorialPage("📅", "Daily Challenge", "A new puzzle every day. Win to extend your streak and earn a bonus reward. Good luck!")
)

@Composable
fun TutorialScreen(navController: NavHostController) {
    var page by remember { mutableIntStateOf(0) }
    val current = PAGES[page]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Skip button top-right
        TextButton(
            onClick = {
                navController.navigate(NavigationRoute.Home.route) {
                    popUpTo(NavigationRoute.Tutorial.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text("Skip", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(current.emoji, fontSize = 72.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                current.title,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    current.body,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Page dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PAGES.indices.forEach { i ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (i == page) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(if (i == page) 10.dp else 8.dp)
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (page < PAGES.lastIndex) {
                Button(
                    onClick = { page++ },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Next →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        navController.navigate(NavigationRoute.Home.route) {
                            popUpTo(NavigationRoute.Tutorial.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("🎮 Start Playing!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
