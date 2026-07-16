package fr.vferries.cuisine.data.timers

import android.content.Intent

/** Cible de navigation portée par le tap sur une notif de timer. */
data class TimerDeepLink(
    val slug: String,
    val sectionIdx: Int,
    val stepIdx: Int,
)

fun timerDeepLinkFrom(intent: Intent): TimerDeepLink? {
    val slug = intent.getStringExtra(TimerNotifier.EXTRA_DEEPLINK_SLUG) ?: return null
    val sectionIdx = intent.getIntExtra(TimerNotifier.EXTRA_DEEPLINK_SECTION, -1)
    val stepIdx = intent.getIntExtra(TimerNotifier.EXTRA_DEEPLINK_STEP, -1)
    if (sectionIdx < 0 || stepIdx < 0) return null
    return TimerDeepLink(slug, sectionIdx, stepIdx)
}
