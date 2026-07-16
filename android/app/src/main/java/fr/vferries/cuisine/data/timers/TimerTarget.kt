package fr.vferries.cuisine.data.timers

/** Cible d'un timer, encodée dans son id : "slug:sectionIdx:stepIdx:tokIdx". */
data class TimerTarget(
    val slug: String,
    val sectionIdx: Int,
    val stepIdx: Int,
    val tokIdx: Int,
)

fun parseTimerId(id: String): TimerTarget? {
    val parts = id.split(":")
    if (parts.size != 4 || parts[0].isBlank()) return null
    val sectionIdx = parts[1].toIntOrNull() ?: return null
    val stepIdx = parts[2].toIntOrNull() ?: return null
    val tokIdx = parts[3].toIntOrNull() ?: return null
    return TimerTarget(parts[0], sectionIdx, stepIdx, tokIdx)
}
