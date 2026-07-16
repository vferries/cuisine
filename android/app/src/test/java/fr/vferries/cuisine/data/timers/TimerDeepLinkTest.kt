package fr.vferries.cuisine.data.timers

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TimerDeepLinkTest {

    @Test fun reads_a_complete_deeplink_intent() {
        val intent = Intent().apply {
            putExtra(TimerNotifier.EXTRA_DEEPLINK_SLUG, "porc")
            putExtra(TimerNotifier.EXTRA_DEEPLINK_SECTION, 0)
            putExtra(TimerNotifier.EXTRA_DEEPLINK_STEP, 1)
        }

        assertEquals(TimerDeepLink("porc", 0, 1), timerDeepLinkFrom(intent))
    }

    @Test fun returns_null_without_slug() {
        assertNull(timerDeepLinkFrom(Intent()))
    }

    @Test fun returns_null_when_indices_are_missing() {
        val intent = Intent().apply {
            putExtra(TimerNotifier.EXTRA_DEEPLINK_SLUG, "porc")
        }

        assertNull(timerDeepLinkFrom(intent))
    }
}
