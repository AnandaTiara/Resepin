package com.kelompok4.resepin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Surface,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = PrimaryDark,
    secondary = Primary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = Surface,
    surfaceContainerHigh = Surface,
    surfaceContainerHighest = SurfaceVariant,
    outline = Border,
    outlineVariant = DividerColor,
    error = Danger,
)

@Composable
fun ResepinTheme(content: @Composable () -> Unit) {
    // Desain hanya menyediakan mode terang, jadi tema selalu terang.
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content,
    )
}
