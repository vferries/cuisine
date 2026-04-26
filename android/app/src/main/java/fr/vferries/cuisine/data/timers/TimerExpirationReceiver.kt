package fr.vferries.cuisine.data.timers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Reçu par l'OS quand un timer expire (alarme exacte programmée par TimerScheduler).
 * Postera la notification chronomètre + son. Stub pour l'instant — voir task #3.
 */
class TimerExpirationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Task #3 : post notification avec setUsesChronometer/setChronometerCountDown.
    }
}
