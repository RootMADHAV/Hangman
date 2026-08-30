package com.LetterQuest.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

import com.LetterQuest.domain.model.GameState

@Composable
fun SoulCandleVisual(
    wrongGuesses: Int,
    maxAttempts: Int,
    gameState: GameState,
    modifier: Modifier = Modifier,
    onWrongGuessShake: () -> Unit = {},
    onCorrectGuessPulse: () -> Unit = {}
) {
    val view = LocalView.current
    val remaining = maxAttempts - wrongGuesses
    val fraction = remaining.toFloat() / maxAttempts

    var shakeOffset by remember { mutableStateOf(Offset.Zero) }
    var showParticles by remember { mutableStateOf(false) }
    var particles by remember { mutableStateOf(listOf<Particle>()) }
    var pulseScale by remember { mutableFloatStateOf(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "soul_candle")
    val flicker by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flicker"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(wrongGuesses) {
        if (wrongGuesses > 0) {
            shakeOffset = Offset(Random.nextFloat() * 12f - 6f, 0f)
            delay(50)
            shakeOffset = Offset.Zero
            onWrongGuessShake()
        }
    }

    LaunchedEffect(gameState) {
        if (gameState == GameState.WON) {
            repeat(12) {
                particles = particles + List(8) { i ->
                    Particle(
                        x = Random.nextFloat(),
                        y = 0.5f,
                        vx = (Random.nextFloat() - 0.5f) * 0.15f,
                        vy = -Random.nextFloat() * 0.2f - 0.1f,
                        life = 1f,
                        color = if (Random.nextBoolean()) Color(0xFFFFD700) else Color(0xFFFF6B35)
                    )
                }
                delay(80)
            }
            showParticles = true
            delay(1500)
            showParticles = false
            particles = emptyList()
        }
    }

    LaunchedEffect(gameState) {
        if (gameState == GameState.WON || gameState == GameState.LOST) {
            val targetScale = if (gameState == GameState.WON) 1.15f else 0.8f
            pulseScale = targetScale
        } else {
            pulseScale = 1f
        }
    }

    LaunchedEffect(gameState) {
        if (gameState == GameState.WON) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
        }
        if (gameState == GameState.LOST) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
        }
    }

    val scale by animateFloatAsState(
        targetValue = pulseScale,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 200f),
        label = "scale"
    )

    val flameColor = when {
        gameState == GameState.LOST -> Color(0xFF2C3E50)
        fraction > 0.66f -> Color(0xFFFFD700)
        fraction > 0.33f -> Color(0xFFFF8C00)
        else -> Color(0xFFFF4500)
    }

    val glowColor = when {
        gameState == GameState.LOST -> Color(0xFF1a1a2e).copy(alpha = 0.3f)
        fraction > 0.66f -> Color(0xFFFFD700).copy(alpha = 0.4f)
        fraction > 0.33f -> Color(0xFFFF8C00).copy(alpha = 0.35f)
        else -> Color(0xFFFF4500).copy(alpha = 0.3f)
    }

    Box(
        modifier = modifier
            .offset { IntOffset(shakeOffset.x.roundToInt(), shakeOffset.y.roundToInt()) }
            .drawWithContent {
                drawContent()

                if (showParticles) {
                    particles.forEach { p ->
                        val alpha = p.life
                        val radius = 4f * p.life
                        drawCircle(
                            color = p.color.copy(alpha = alpha),
                            radius = radius,
                            center = Offset(p.x * size.width, p.y * size.height)
                        )
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val baseY = h * 0.75f

            if (gameState != GameState.LOST) {
                drawCircle(
                    color = glowColor,
                    radius = w * 0.35f * glowPulse,
                    center = Offset(centerX, baseY - h * 0.15f),
                    alpha = if (gameState == GameState.WON) 0.6f else 0.4f
                )
            }

            drawCircle(
                color = Color(0xFF2C3E50).copy(alpha = 0.15f),
                radius = w * 0.28f,
                center = Offset(centerX, baseY),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            val progressAngle = 360f * (1f - fraction)
            if (progressAngle > 0f) {
                drawArc(
                    color = flameColor.copy(alpha = 0.6f),
                    startAngle = -90f,
                    sweepAngle = progressAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - w * 0.28f, baseY - h * 0.28f),
                    size = Size(w * 0.56f, h * 0.56f),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            val flameBaseY = baseY - h * 0.08f
            val flameMaxHeight = h * 0.45f * flicker * scale
            val flameWidth = w * 0.12f * scale

            val flamePath = Path().apply {
                moveTo(centerX - flameWidth, flameBaseY)
                cubicTo(
                    centerX - flameWidth * 0.6f, flameBaseY - flameMaxHeight * 0.5f,
                    centerX - flameWidth * 0.3f, flameBaseY - flameMaxHeight * 0.8f,
                    centerX, flameBaseY - flameMaxHeight
                )
                cubicTo(
                    centerX + flameWidth * 0.3f, flameBaseY - flameMaxHeight * 0.8f,
                    centerX + flameWidth * 0.6f, flameBaseY - flameMaxHeight * 0.5f,
                    centerX + flameWidth, flameBaseY
                )
                close()
            }

            drawPath(
                path = flamePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        flameColor.copy(alpha = 0.8f),
                        flameColor.copy(alpha = 0.3f)
                    ),
                    startY = flameBaseY - flameMaxHeight,
                    endY = flameBaseY
                )
            )

            drawCircle(
                color = Color(0xFF2C3E50),
                radius = w * 0.08f,
                center = Offset(centerX, flameBaseY + h * 0.04f)
            )
            drawCircle(
                color = Color(0xFF34495E),
                radius = w * 0.05f,
                center = Offset(centerX, flameBaseY + h * 0.04f)
            )
            drawCircle(
                color = Color(0xFF7F8C8D),
                radius = w * 0.025f,
                center = Offset(centerX, flameBaseY + h * 0.04f)
            )
        }
    }
}

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val life: Float,
    val color: Color
)

data class SmokeParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val life: Float,
    val size: Float
)

@Composable
fun SoulCandleVisualDeprecated(
    wrongGuesses: Int,
    maxAttempts: Int,
    modifier: Modifier = Modifier
) {
    val remaining = maxAttempts - wrongGuesses
    val fraction = remaining.toFloat() / maxAttempts

    val infiniteTransition = rememberInfiniteTransition(label = "flame")
    val flicker by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flicker"
    )

    val flameColor = when {
        fraction > 0.66f -> Color(0xFFFFD700)
        fraction > 0.33f -> Color(0xFFFF8C00)
        else -> Color(0xFFFF4500)
    }

    val glowColor = flameColor.copy(alpha = 0.3f)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val baseY = h * 0.7f

            drawCircle(
                color = glowColor,
                radius = w * 0.3f,
                center = Offset(centerX, baseY - h * 0.2f),
                alpha = 0.4f
            )

            drawCircle(
                color = Color(0xFF2C3E50).copy(alpha = 0.15f),
                radius = w * 0.25f,
                center = Offset(centerX, baseY),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            val progressAngle = 360f * (1f - fraction)
            if (progressAngle > 0f) {
                drawArc(
                    color = flameColor.copy(alpha = 0.5f),
                    startAngle = -90f,
                    sweepAngle = progressAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - w * 0.25f, baseY - h * 0.25f),
                    size = Size(w * 0.5f, h * 0.5f),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            val flameHeight = h * 0.4f * flicker * fraction
            val flameWidth = w * 0.1f
            val flamePath = Path().apply {
                moveTo(centerX - flameWidth, baseY)
                cubicTo(
                    centerX - flameWidth * 0.5f, baseY - flameHeight * 0.5f,
                    centerX - flameWidth * 0.2f, baseY - flameHeight * 0.8f,
                    centerX, baseY - flameHeight
                )
                cubicTo(
                    centerX + flameWidth * 0.2f, baseY - flameHeight * 0.8f,
                    centerX + flameWidth * 0.5f, baseY - flameHeight * 0.5f,
                    centerX + flameWidth, baseY
                )
                close()
            }

            drawPath(
                path = flamePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        flameColor.copy(alpha = 0.7f),
                        flameColor.copy(alpha = 0.2f)
                    ),
                    startY = baseY - flameHeight,
                    endY = baseY
                )
            )

            drawCircle(
                color = Color(0xFF2C3E50),
                radius = w * 0.06f,
                center = Offset(centerX, baseY + h * 0.05f)
            )
            drawCircle(
                color = Color(0xFF7F8C8D),
                radius = w * 0.025f,
                center = Offset(centerX, baseY + h * 0.05f)
            )
        }
    }
}
