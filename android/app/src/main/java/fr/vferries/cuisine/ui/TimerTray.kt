package fr.vferries.cuisine.ui

import android.media.MediaPlayer
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.vferries.cuisine.R
import fr.vferries.cuisine.data.timers.RunningTimer
import fr.vferries.cuisine.data.timers.TimerRegistry
import fr.vferries.cuisine.data.timers.formatRemaining
import fr.vferries.cuisine.data.timers.isExpired
import fr.vferries.cuisine.data.timers.remainingSeconds
import kotlinx.coroutines.delay

@Composable
fun TimerTrayOverlay() {
    val timers by TimerRegistry.state.collectAsState()
    val context = LocalContext.current

    // Timers déjà notifiés (son joué une fois). Pré-rempli avec les expirés au
    // chargement pour qu'un timer qui a expiré pendant une session précédente
    // ne bippe pas en rouvrant l'app.
    val notified = remember {
        mutableStateOf(
            timers.filter { it.isExpired(System.currentTimeMillis()) }.map { it.id }.toSet(),
        )
    }

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = System.currentTimeMillis()
        }
    }

    // Détecter les transitions running → expired et jouer le beep une fois.
    LaunchedEffect(timers, now) {
        val freshlyExpired = timers.filter { it.isExpired(now) && it.id !in notified.value }
        if (freshlyExpired.isNotEmpty()) {
            playBeep(context)
            notified.value = notified.value + freshlyExpired.map { it.id }
        }
        // Nettoyer les ids qui ne sont plus dans la liste (dismiss).
        val currentIds = timers.map { it.id }.toSet()
        val stale = notified.value - currentIds
        if (stale.isNotEmpty()) notified.value = notified.value - stale
    }

    if (timers.isEmpty()) return

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

    val tint = MaterialTheme.colorScheme.surfaceVariant
    val accent = MaterialTheme.colorScheme.primary
    val fgTint = MaterialTheme.colorScheme.onSurfaceVariant
    val fgAccent = MaterialTheme.colorScheme.onPrimary

    val transition = rememberInfiniteTransition(label = "timer-blink")
    val phase by transition.animateFloat(
        label = "phase",
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val bg = if (expired) lerp(tint, accent, phase) else tint
    val fg = if (expired) lerp(fgTint, fgAccent, phase) else fgTint

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

private fun playBeep(context: android.content.Context) {
    runCatching {
        val mp = MediaPlayer.create(context, R.raw.timer_beep) ?: return
        mp.setOnCompletionListener { it.release() }
        mp.start()
    }
}
