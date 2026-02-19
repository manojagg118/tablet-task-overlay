package com.example.tablettaskoverlay.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    secondary = TealSecondary,
    background = LightBg
)

private val DarkColors = darkColorScheme(
    primary = BluePrimary,
    secondary = TealSecondary,
    background = DarkBg
)

@Composable
fun TabletTaskOverlayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
