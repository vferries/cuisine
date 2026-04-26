package fr.vferries.cuisine.data.timers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Programme un broadcast à l'expiration d'un timer via AlarmManager.
 * Survit au kill de l'app : c'est l'OS qui réveille le device et tire l'alarme.
 *
 * Sur Android 12+, si SCHEDULE_EXACT_ALARM n'est pas accordée, retombe sur
 * setAndAllowWhileIdle (inexact, peut glisser de quelques minutes en doze).
 */
class TimerScheduler(context: Context) {

    private val appContext: Context = context.applicationContext
    private val alarms: AlarmManager =
        appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(timer: RunningTimer) {
        val triggerAt = timer.startedAtMillis + timer.durationSeconds * 1000L
        val pi = pendingIntentFor(timer.id, create = true)!!
        if (canScheduleExact()) {
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(id: String) {
        val pi = pendingIntentFor(id, create = false) ?: return
        alarms.cancel(pi)
        pi.cancel()
    }

    private fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()

    private fun pendingIntentFor(id: String, create: Boolean): PendingIntent? {
        val intent = Intent(appContext, TimerExpirationReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_TIMER_ID, id)
        }
        val flags = (if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE) or
            PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(appContext, id.hashCode(), intent, flags)
    }

    companion object {
        const val ACTION_FIRE = "fr.vferries.cuisine.timer.FIRE"
        const val EXTRA_TIMER_ID = "timer_id"
    }
}
