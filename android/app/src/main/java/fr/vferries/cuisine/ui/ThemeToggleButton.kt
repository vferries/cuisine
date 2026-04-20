package fr.vferries.cuisine.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vferries.cuisine.ui.theme.ThemeMode

@Composable
fun ThemeToggleButton(
    mode: ThemeMode,
    onCycle: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        FilledTonalIconButton(
            onClick = onCycle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp),
        ) {
            Text(text = when (mode) {
                ThemeMode.SYSTEM -> "◐"
                ThemeMode.LIGHT -> "☀"
                ThemeMode.DARK -> "🌙"
            })
        }
    }
}
