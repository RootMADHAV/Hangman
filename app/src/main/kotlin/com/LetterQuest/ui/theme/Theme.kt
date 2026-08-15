package com.LetterQuest.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.LetterQuest.domain.model.ThemeColors

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    outline = Outline
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    outline = DarkOutline
)

private fun longToColor(colorLong: Long): Color = Color(colorLong.toULong())

private fun createColorScheme(themeColors: ThemeColors, isDark: Boolean): androidx.compose.material3.ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = longToColor(themeColors.primary),
        onPrimary = longToColor(themeColors.onPrimary),
        primaryContainer = longToColor(themeColors.primaryContainer),
        onPrimaryContainer = longToColor(themeColors.onPrimaryContainer),
        secondary = longToColor(themeColors.secondary),
        onSecondary = longToColor(themeColors.onSecondary),
        secondaryContainer = longToColor(themeColors.secondaryContainer),
        onSecondaryContainer = longToColor(themeColors.onSecondaryContainer),
        tertiary = longToColor(themeColors.tertiary),
        onTertiary = longToColor(themeColors.onTertiary),
        tertiaryContainer = longToColor(themeColors.tertiaryContainer),
        onTertiaryContainer = longToColor(themeColors.onTertiaryContainer),
        error = longToColor(themeColors.error),
        onError = longToColor(themeColors.onError),
        errorContainer = longToColor(themeColors.errorContainer),
        onErrorContainer = longToColor(themeColors.onErrorContainer),
        background = longToColor(themeColors.background),
        onBackground = longToColor(themeColors.onBackground),
        surface = longToColor(themeColors.surface),
        onSurface = longToColor(themeColors.onSurface),
        outline = longToColor(themeColors.outline)
    )
}

@Composable
fun HangmanGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    customColors: ThemeColors? = null,
    customIsDark: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (customColors != null) {
        createColorScheme(customColors, customIsDark)
    } else {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
