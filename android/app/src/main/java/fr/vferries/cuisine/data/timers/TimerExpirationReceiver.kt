package fr.vferries.cuisine.data.timers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Reçu par l'OS à l'expiration d'un timer ; poste la notif sonore. */
class TimerExpirationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(TimerScheduler.EXTRA_TIMER_ID) ?: return
        val name = intent.getStringExtra(TimerScheduler.EXTRA_TIMER_NAME).orEmpty()
        TimerNotifier(context).notifyExpired(id, name)
    }
}
