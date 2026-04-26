package fr.vferries.cuisine.data.timers

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TimerExpirationReceiverTest {

    @Test fun onReceive_posts_an_expiration_notification() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val notif = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(TimerScheduler.ACTION_FIRE).apply {
            putExtra(TimerScheduler.EXTRA_TIMER_ID, "porc:0:1:0")
            putExtra(TimerScheduler.EXTRA_TIMER_NAME, "Cuisson")
            putExtra(TimerScheduler.EXTRA_TIMER_END_TIME, 123_456L)
        }

        TimerExpirationReceiver().onReceive(ctx, intent)

        val active = shadowOf(notif).activeNotifications
        assertEquals(1, active.size)
        val n = active.first().notification
        assertNotNull(n)
        val title = n.extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()
        assertTrue(
            "Le titre doit mentionner le nom du timer (était: $title)",
            title?.contains("Cuisson") == true,
        )
    }
}
