package fr.vferries.cuisine.data.timers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import fr.vferries.cuisine.R

/**
 * Poste les notifications d'expiration de timer.
 * Le son et la priorité sont portés par le channel ; à l'expiration, l'OS joue
 * le beep même si l'app n'est plus en RAM.
 */
class TimerNotifier(context: Context) {

    private val appContext: Context = context.applicationContext
    private val nm: NotificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        ensureChannel()
    }

    fun notifyExpired(id: String, name: String) {
        val title = if (name.isNotBlank()) "$name terminé" else "Timer terminé"
        val notif = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle(title)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(id.hashCode(), notif)
        Log.d(TAG, "notifyExpired posted id=$id name='$name'")
    }

    private fun ensureChannel() {
        val sound = Uri.parse("android.resource://${appContext.packageName}/raw/timer_beep")
        val existing = nm.getNotificationChannel(CHANNEL_ID)
        if (existing != null && existing.sound != null) {
            Log.d(TAG, "channel exists, sound=${existing.sound}")
            return
        }
        if (existing != null) {
            Log.d(TAG, "channel exists but sound is null — recreating")
            nm.deleteNotificationChannel(CHANNEL_ID)
        }
        val attrs = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Timers de cuisson",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Bip à l'expiration d'un timer de cuisson"
            setSound(sound, attrs)
            enableVibration(true)
        }
        nm.createNotificationChannel(channel)
        Log.d(TAG, "channel created sound=$sound")
    }

    companion object {
        const val CHANNEL_ID = "timers"
        private const val TAG = "Cuisine.Timers"
    }
}
