package fr.vferries.cuisine.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RandomTest {

    @Test
    fun returns_null_on_empty_list() {
        assertNull(pickRandom(emptyList<String>()) { 0.0 })
    }

    @Test
    fun rng_zero_returns_first_element() {
        assertEquals("a", pickRandom(listOf("a", "b", "c")) { 0.0 })
    }

    @Test
    fun rng_almost_one_returns_last_element() {
        assertEquals("c", pickRandom(listOf("a", "b", "c")) { 0.999 })
    }

    @Test
    fun rng_half_on_four_elements_returns_third() {
        assertEquals("c", pickRandom(listOf("a", "b", "c", "d")) { 0.5 })
    }
}
