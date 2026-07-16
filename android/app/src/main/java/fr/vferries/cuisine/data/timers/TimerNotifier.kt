package fr.vferries.cuisine.data.timers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import fr.vferries.cuisine.MainActivity
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
        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle(title)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        val target = parseTimerId(id)
        if (target != null) {
            builder.setContentIntent(deepLinkIntent(id, target))
        } else {
            Log.w(TAG, "notifyExpired: id non parsable '$id' — notif sans deep-link")
        }
        nm.notify(id.hashCode(), builder.build())
        Log.d(TAG, "notifyExpired posted id=$id name='$name' deepLink=${target != null}")
    }

    /** Tap sur la notif → MainActivity avec la cible ; un PendingIntent par timer. */
    private fun deepLinkIntent(id: String, target: TimerTarget): PendingIntent {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            putExtra(EXTRA_DEEPLINK_SLUG, target.slug)
            putExtra(EXTRA_DEEPLINK_SECTION, target.sectionIdx)
            putExtra(EXTRA_DEEPLINK_STEP, target.stepIdx)
        }
        return PendingIntent.getActivity(
            appContext,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
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
        const val EXTRA_DEEPLINK_SLUG = "deeplink_slug"
        const val EXTRA_DEEPLINK_SECTION = "deeplink_section"
        const val EXTRA_DEEPLINK_STEP = "deeplink_step"
        private const val TAG = "Cuisine.Timers"
    }
}
