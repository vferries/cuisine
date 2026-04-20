package fr.vferries.cuisine.data.timers

import kotlinx.serialization.Serializable

@Serializable
data class RunningTimer(
    val id: String,
    val name: String,
    val durationSeconds: Int,
    val startedAtMillis: Long,
)

fun RunningTimer.remainingSeconds(nowMillis: Long): Long {
    val elapsed = (nowMillis - startedAtMillis) / 1000L
    return durationSeconds - elapsed
}

fun RunningTimer.isExpired(nowMillis: Long): Boolean = remainingSeconds(nowMillis) <= 0

fun formatRemaining(seconds: Long): String {
    val clamped = seconds.coerceAtLeast(0)
    val m = clamped / 60
    val s = clamped % 60
    return "%d:%02d".format(m, s)
}

/** Convertit (quantité texte, unité) en secondes. Unités : sec, min, h. */
fun timerDurationSeconds(quantity: String, unit: String): Int {
    val q = quantity.toDoubleOrNull() ?: return 0
    val multiplier = when (unit) {
        "sec" -> 1
        "min" -> 60
        "h" -> 3600
        else -> 0
    }
    return (q * multiplier).toInt()
}
