package fr.vferries.cuisine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import fr.vferries.cuisine.data.timers.RunningTimer
import fr.vferries.cuisine.data.timers.TimerRegistry
import fr.vferries.cuisine.data.timers.formatRemaining
import fr.vferries.cuisine.data.timers.isExpired
import fr.vferries.cuisine.data.timers.remainingSeconds
import kotlinx.coroutines.delay

@Composable
fun TimerTrayOverlay() {
    val timers by TimerRegistry.state.collectAsState()
    if (timers.isEmpty()) return

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = System.currentTimeMillis()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            timers.forEach { t -> TimerItem(t, now) }
        }
    }
}

@Composable
private fun TimerItem(t: RunningTimer, now: Long) {
    val remaining = t.remainingSeconds(now)
    val expired = t.isExpired(now)
    val bg = if (expired) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (expired) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
    ) {
        Text(text = t.name, color = fg, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = formatRemaining(remaining),
            color = fg,
            style = MaterialTheme.typography.bodyMedium,
        )
        IconButton(onClick = { TimerRegistry.stop(t.id) }) {
            Text(text = "✕", color = fg)
        }
    }
}

