package fr.vferries.cuisine.data.favorites

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FavoritesStoreTest {

    private lateinit var store: FavoritesStore

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        store = FavoritesStore.from(ctx)
        store.clear()
    }

    @Test fun get_returns_empty_initially() {
        assertEquals(emptySet<String>(), store.get())
    }

    @Test fun toggle_adds_then_removes() {
        store.toggle("porc")
        assertEquals(setOf("porc"), store.get())
        assertTrue(store.contains("porc"))

        store.toggle("porc")
        assertEquals(emptySet<String>(), store.get())
        assertFalse(store.contains("porc"))
    }

    @Test fun toggle_multiple_slugs_independent() {
        store.toggle("porc")
        store.toggle("tatin")
        assertEquals(setOf("porc", "tatin"), store.get())

        store.toggle("porc")
        assertEquals(setOf("tatin"), store.get())
    }

    @Test fun clear_removes_all() {
        store.toggle("porc")
        store.toggle("tatin")
        store.clear()
        assertEquals(emptySet<String>(), store.get())
    }
}
