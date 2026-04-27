package fr.vferries.cuisine.data

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ShareIntentTest {

    @Test
    fun build_share_intent_carries_title_and_url_as_plain_text() {
        val intent = buildShareIntent(title = "Porc noir", url = "https://x/porc")
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals("Porc noir", intent.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals(
            "Porc noir\nhttps://x/porc",
            intent.getStringExtra(Intent.EXTRA_TEXT),
        )
    }
}
