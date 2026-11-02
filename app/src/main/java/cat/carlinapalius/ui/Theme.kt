package cat.carlinapalius.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF2E6B4F)
private val Cream = Color(0xFFFFF4D6)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEBD9),
    onPrimaryContainer = Color(0xFF0B2A1D),
    secondaryContainer = Cream,
    onSecondaryContainer = Color(0xFF3A2E10),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9BD3B5),
    onPrimary = Color(0xFF0B2A1D),
    primaryContainer = Color(0xFF1F4D38),
    onPrimaryContainer = Color(0xFFCDEBD9),
    secondaryContainer = Color(0xFF4A3F22),
    onSecondaryContainer = Cream,
)

@Composable
fun CarlinaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
