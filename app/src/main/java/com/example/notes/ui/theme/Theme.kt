package com.example.notes.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme by lazy {
    darkColorScheme(
        primary = PrimaryDark,
        onPrimary = OnPrimaryDark,
        primaryContainer = PrimaryContainerDark,
        onPrimaryContainer = OnPrimaryContainerDark,
        secondary = SecondaryDark,
        onSecondary = OnSecondaryDark,
        secondaryContainer = SecondaryContainerDark,
        onSecondaryContainer = OnSecondaryContainerDark,
        tertiary = TertiaryDark,
        onTertiary = OnTertiaryDark,
        tertiaryContainer = TertiaryContainerDark,
        onTertiaryContainer = OnTertiaryContainerDark,
        background = BackgroundDark,
        onBackground = OnBackgroundDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = OnSurfaceVariantDark,
        outline = OutlineDark,
        outlineVariant = OutlineVariantDark
    )
}

private val LightColorScheme by lazy {
    lightColorScheme(
        primary = PrimaryLight,
        onPrimary = OnPrimaryLight,
        primaryContainer = PrimaryContainerLight,
        onPrimaryContainer = OnPrimaryContainerLight,
        secondary = SecondaryLight,
        onSecondary = OnSecondaryLight,
        secondaryContainer = SecondaryContainerLight,
        onSecondaryContainer = OnSecondaryContainerLight,
        tertiary = TertiaryLight,
        onTertiary = OnTertiaryLight,
        tertiaryContainer = TertiaryContainerLight,
        onTertiaryContainer = OnTertiaryContainerLight,
        error = ErrorLight,
        onError = OnErrorLight,
        errorContainer = ErrorContainerLight,
        onErrorContainer = OnErrorContainerLight,
        background = BackgroundLight,
        onBackground = OnBackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = OnSurfaceVariantLight,
        outline = OutlineLight,
        outlineVariant = OutlineVariantLight
    )
}

@Composable
fun NotesTheme(
    themeMode: String = "LIGHT", // Default to LIGHT for the Lavender look
    fontScale: Float = 1.0f,
    useSystemFontSize: Boolean = true,
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce the Lavender palette
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Применяем масштабирование шрифта, сохраняя ВСЕ параметры оригинального стиля
    val currentTypography = AppTypography
    val finalFontScale = if (useSystemFontSize) 1.0f else fontScale

    val scaledTypography = Typography(
        displayLarge = currentTypography.displayLarge.copy(fontSize = currentTypography.displayLarge.fontSize * finalFontScale),
        displayMedium = currentTypography.displayMedium.copy(fontSize = currentTypography.displayMedium.fontSize * finalFontScale),
        displaySmall = currentTypography.displaySmall.copy(fontSize = currentTypography.displaySmall.fontSize * finalFontScale),
        headlineLarge = currentTypography.headlineLarge.copy(fontSize = currentTypography.headlineLarge.fontSize * finalFontScale),
        headlineMedium = currentTypography.headlineMedium.copy(fontSize = currentTypography.headlineMedium.fontSize * finalFontScale),
        headlineSmall = currentTypography.headlineSmall.copy(fontSize = currentTypography.headlineSmall.fontSize * finalFontScale),
        titleLarge = currentTypography.titleLarge.copy(fontSize = currentTypography.titleLarge.fontSize * finalFontScale),
        titleMedium = currentTypography.titleMedium.copy(fontSize = currentTypography.titleMedium.fontSize * finalFontScale),
        titleSmall = currentTypography.titleSmall.copy(fontSize = currentTypography.titleSmall.fontSize * finalFontScale),
        bodyLarge = currentTypography.bodyLarge.copy(fontSize = currentTypography.bodyLarge.fontSize * finalFontScale),
        bodyMedium = currentTypography.bodyMedium.copy(fontSize = currentTypography.bodyMedium.fontSize * finalFontScale),
        bodySmall = currentTypography.bodySmall.copy(fontSize = currentTypography.bodySmall.fontSize * finalFontScale),
        labelLarge = currentTypography.labelLarge.copy(fontSize = currentTypography.labelLarge.fontSize * finalFontScale),
        labelMedium = currentTypography.labelMedium.copy(fontSize = currentTypography.labelMedium.fontSize * finalFontScale),
        labelSmall = currentTypography.labelSmall.copy(fontSize = currentTypography.labelSmall.fontSize * finalFontScale)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
