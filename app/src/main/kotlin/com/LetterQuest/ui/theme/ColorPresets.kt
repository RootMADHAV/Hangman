package com.LetterQuest.ui.theme

import androidx.compose.ui.graphics.Color
import com.LetterQuest.domain.model.ThemePreset
import com.LetterQuest.domain.model.ThemeColors

private fun colorToLong(color: Color): Long = color.value.toLong()

private fun longToColor(colorLong: Long): Color = Color(colorLong.toULong())

private fun createThemeColors(
    primary: Color,
    onPrimary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    secondary: Color,
    onSecondary: Color,
    secondaryContainer: Color,
    onSecondaryContainer: Color,
    tertiary: Color,
    onTertiary: Color,
    tertiaryContainer: Color,
    onTertiaryContainer: Color,
    error: Color,
    onError: Color,
    errorContainer: Color,
    onErrorContainer: Color,
    background: Color,
    onBackground: Color,
    surface: Color,
    onSurface: Color,
    outline: Color
) = ThemeColors(
    primary = colorToLong(primary),
    onPrimary = colorToLong(onPrimary),
    primaryContainer = colorToLong(primaryContainer),
    onPrimaryContainer = colorToLong(onPrimaryContainer),
    secondary = colorToLong(secondary),
    onSecondary = colorToLong(onSecondary),
    secondaryContainer = colorToLong(secondaryContainer),
    onSecondaryContainer = colorToLong(onSecondaryContainer),
    tertiary = colorToLong(tertiary),
    onTertiary = colorToLong(onTertiary),
    tertiaryContainer = colorToLong(tertiaryContainer),
    onTertiaryContainer = colorToLong(onTertiaryContainer),
    error = colorToLong(error),
    onError = colorToLong(onError),
    errorContainer = colorToLong(errorContainer),
    onErrorContainer = colorToLong(onErrorContainer),
    background = colorToLong(background),
    onBackground = colorToLong(onBackground),
    surface = colorToLong(surface),
    onSurface = colorToLong(onSurface),
    outline = colorToLong(outline)
)

object ColorPresets {
    val lightPreset = ThemePreset(
        id = "light",
        name = "Light",
        isDark = false,
        colors = createThemeColors(
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
    )

    val darkPreset = ThemePreset(
        id = "dark",
        name = "Dark",
        isDark = true,
        colors = createThemeColors(
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
    )

    // ── Premium themes (purchasable with tokens) ──────────────────────────────

    val midnightOceanPreset = ThemePreset(
        id = "midnight_ocean",
        name = "Midnight Ocean",
        isDark = true,
        cost = 300,
        colors = createThemeColors(
            primary = Color(0xFF1A7EBD),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF0D4F7A),
            onPrimaryContainer = Color(0xFFB3D9F5),
            secondary = Color(0xFF00B4D8),
            onSecondary = Color(0xFF002B36),
            secondaryContainer = Color(0xFF004F5E),
            onSecondaryContainer = Color(0xFF9AEAF5),
            tertiary = Color(0xFF0096C7),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF003F52),
            onTertiaryContainer = Color(0xFF90E0EF),
            error = Color(0xFFCF6679),
            onError = Color(0xFF370015),
            errorContainer = Color(0xFF5C1427),
            onErrorContainer = Color(0xFFFFB3C0),
            background = Color(0xFF0A1628),
            onBackground = Color(0xFFD0E8F5),
            surface = Color(0xFF0D1F3C),
            onSurface = Color(0xFFD0E8F5),
            outline = Color(0xFF2A5070)
        )
    )

    val sunsetFirePreset = ThemePreset(
        id = "sunset_fire",
        name = "Sunset Fire",
        isDark = true,
        cost = 300,
        colors = createThemeColors(
            primary = Color(0xFFE05C00),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF7A2F00),
            onPrimaryContainer = Color(0xFFFFD5B8),
            secondary = Color(0xFFFFB300),
            onSecondary = Color(0xFF3B2700),
            secondaryContainer = Color(0xFF5C3A00),
            onSecondaryContainer = Color(0xFFFFD966),
            tertiary = Color(0xFFFF6B35),
            onTertiary = Color(0xFF3B0D00),
            tertiaryContainer = Color(0xFF7A2100),
            onTertiaryContainer = Color(0xFFFFB59A),
            error = Color(0xFFFF5449),
            onError = Color(0xFF3B0007),
            errorContainer = Color(0xFF690015),
            onErrorContainer = Color(0xFFFFDAD7),
            background = Color(0xFF2B1200),
            onBackground = Color(0xFFFFD5B8),
            surface = Color(0xFF3D1A00),
            onSurface = Color(0xFFFFD5B8),
            outline = Color(0xFF7A3E00)
        )
    )

    val cottonCandyPreset = ThemePreset(
        id = "cotton_candy",
        name = "Cotton Candy",
        isDark = false,
        cost = 400,
        colors = createThemeColors(
            primary = Color(0xFFD63FAB),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFD6EE),
            onPrimaryContainer = Color(0xFF5A0040),
            secondary = Color(0xFF9C4DCC),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF3D9FF),
            onSecondaryContainer = Color(0xFF300050),
            tertiary = Color(0xFF00BCD4),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFCCF5FA),
            onTertiaryContainer = Color(0xFF003040),
            error = Color(0xFFD32F2F),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD7),
            onErrorContainer = Color(0xFF3B0007),
            background = Color(0xFFFFF0FA),
            onBackground = Color(0xFF30003A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF30003A),
            outline = Color(0xFFD4A0CC)
        )
    )

    val neonPulsePreset = ThemePreset(
        id = "neon_pulse",
        name = "Neon Pulse",
        isDark = true,
        cost = 500,
        colors = createThemeColors(
            primary = Color(0xFF39FF14),
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0xFF004500),
            onPrimaryContainer = Color(0xFF8BFFA0),
            secondary = Color(0xFFFF00FF),
            onSecondary = Color(0xFF000000),
            secondaryContainer = Color(0xFF4A004A),
            onSecondaryContainer = Color(0xFFFFAAFF),
            tertiary = Color(0xFF00FFFF),
            onTertiary = Color(0xFF000000),
            tertiaryContainer = Color(0xFF004040),
            onTertiaryContainer = Color(0xFFAAFFFF),
            error = Color(0xFFFF1744),
            onError = Color(0xFF000000),
            errorContainer = Color(0xFF4A0015),
            onErrorContainer = Color(0xFFFFB3C0),
            background = Color(0xFF0A0A0A),
            onBackground = Color(0xFF39FF14),
            surface = Color(0xFF121212),
            onSurface = Color(0xFFE0E0E0),
            outline = Color(0xFF39FF14)
        )
    )

    val arcticIcePreset = ThemePreset(
        id = "arctic_ice",
        name = "Arctic Ice",
        isDark = false,
        cost = 600,
        colors = createThemeColors(
            primary = Color(0xFF00ACC1),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFB3EBF5),
            onPrimaryContainer = Color(0xFF00363D),
            secondary = Color(0xFF00838F),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFB2EBF2),
            onSecondaryContainer = Color(0xFF00363D),
            tertiary = Color(0xFF4DD0E1),
            onTertiary = Color(0xFF000000),
            tertiaryContainer = Color(0xFFE0F7FA),
            onTertiaryContainer = Color(0xFF003636),
            error = Color(0xFFD32F2F),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD7),
            onErrorContainer = Color(0xFF3B0007),
            background = Color(0xFFE0F7FA),
            onBackground = Color(0xFF003636),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF003636),
            outline = Color(0xFF80DEEA)
        )
    )

    val allPresets = listOf(lightPreset, darkPreset,
        midnightOceanPreset, sunsetFirePreset,
        cottonCandyPreset, neonPulsePreset, arcticIcePreset)

    /** IDs of themes that are always free. */
    val FREE_THEME_IDS = setOf("light", "dark")

    fun getPresetById(id: String): ThemePreset? = allPresets.find { it.id == id }
}
