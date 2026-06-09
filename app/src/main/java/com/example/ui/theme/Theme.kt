package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TrustBlue100,
    onPrimary = Slate900,
    primaryContainer = TrustBlue700,
    onPrimaryContainer = TrustBlue100,
    secondary = PeaceGreen100,
    onSecondary = Slate900,
    secondaryContainer = PeaceGreen700,
    onSecondaryContainer = PeaceGreen100,
    tertiary = WarningOrange,
    background = Slate900,
    onBackground = OrganicBeige,
    surface = Slate900,
    onSurface = OrganicBeige,
    surfaceVariant = Slate600,
    onSurfaceVariant = Slate100
)

private val LightColorScheme = lightColorScheme(
    primary = TrustBlue500,
    onPrimary = TrueWhite,
    primaryContainer = TrustBlue700,
    onPrimaryContainer = TrustBlue100,
    secondary = TrustBlue500,
    onSecondary = TrueWhite,
    secondaryContainer = TrustBlue100,
    onSecondaryContainer = TrustBlue700,
    tertiary = WarningOrange,
    background = OrganicBeige,
    onBackground = Slate900,
    surface = TrueWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // We strictly use our custom palette to preserve the "Trust and Peace" look and feel requested by the user
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
