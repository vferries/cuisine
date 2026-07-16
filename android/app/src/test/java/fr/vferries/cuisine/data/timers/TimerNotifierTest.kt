package fr.vferries.cuisine.data.timers

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import fr.vferries.cuisine.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TimerNotifierTest {

    private fun postedNotification(id: String): android.app.Notification {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        TimerNotifier(ctx).notifyExpired(id, "Cuisson")
        return shadowOf(nm).activeNotifications.first().notification
    }

    @Test fun expired_notification_carries_a_deeplink_to_the_step() {
        val n = postedNotification("porc:0:1:2")

        assertNotNull("La notif doit porter un contentIntent", n.contentIntent)
        val saved = shadowOf(n.contentIntent).savedIntent
        assertEquals(MainActivity::class.java.name, saved.component?.className)
        assertEquals("porc", saved.getStringExtra(TimerNotifier.EXTRA_DEEPLINK_SLUG))
        assertEquals(0, saved.getIntExtra(TimerNotifier.EXTRA_DEEPLINK_SECTION, -1))
        assertEquals(1, saved.getIntExtra(TimerNotifier.EXTRA_DEEPLINK_STEP, -1))
    }

    @Test fun malformed_id_yields_a_notification_without_deeplink() {
        val n = postedNotification("id-sans-indices")

        assertNull("Id non parsable → pas de contentIntent", n.contentIntent)
    }
}
