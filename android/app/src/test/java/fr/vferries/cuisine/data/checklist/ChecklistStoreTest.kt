package fr.vferries.cuisine.data.checklist

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ChecklistStoreTest {

    private lateinit var store: ChecklistStore

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        store = ChecklistStore.from(ctx)
        store.clear("porc")
        store.clear("autre")
    }

    @Test fun set_then_get_roundtrip() {
        store.set("porc", setOf("sucre", "piment"))
        assertEquals(setOf("sucre", "piment"), store.get("porc"))
    }

    @Test fun isolated_per_slug() {
        store.set("porc", setOf("sucre"))
        store.set("autre", setOf("tomate"))
        assertEquals(setOf("sucre"), store.get("porc"))
        assertEquals(setOf("tomate"), store.get("autre"))
    }

    @Test fun clear_removes_entries_for_slug() {
        store.set("porc", setOf("sucre"))
        store.clear("porc")
        assertEquals(emptySet<String>(), store.get("porc"))
    }
}
