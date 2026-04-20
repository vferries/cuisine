package fr.vferries.cuisine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFD85A30),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFAECE7),
    onPrimaryContainer = Color(0xFF712B13),
    background = Color(0xFFFAF7F2),
    surface = Color(0xFFFAF7F2),
    surfaceVariant = Color(0xFFF3EFE7),
    onSurface = Color(0xFF1A1A18),
    onSurfaceVariant = Color(0xFF5A5952),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFED7A53),
    onPrimary = Color(0xFF4A1B0C),
    primaryContainer = Color(0xFF3A1E10),
    onPrimaryContainer = Color(0xFFFAECE7),
    background = Color(0xFF14140F),
    surface = Color(0xFF14140F),
    surfaceVariant = Color(0xFF22211C),
    onSurface = Color(0xFFF0EDE6),
    onSurfaceVariant = Color(0xFFA09E93),
)

@Composable
fun CuisineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
