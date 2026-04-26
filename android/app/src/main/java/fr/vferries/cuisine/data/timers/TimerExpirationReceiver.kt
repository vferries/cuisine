package fr.vferries.cuisine.data.timers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/** Reçu par l'OS à l'expiration d'un timer ; poste la notif sonore. */
class TimerExpirationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(TimerScheduler.EXTRA_TIMER_ID)
        val name = intent.getStringExtra(TimerScheduler.EXTRA_TIMER_NAME).orEmpty()
        Log.d(TAG, "onReceive id=$id name='$name'")
        if (id == null) return
        TimerNotifier(context).notifyExpired(id, name)
    }

    private companion object {
        const val TAG = "Cuisine.Timers"
    }
}
