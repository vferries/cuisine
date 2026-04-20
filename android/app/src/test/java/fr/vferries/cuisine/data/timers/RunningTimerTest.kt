package fr.vferries.cuisine.data.timers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningTimerTest {

    private val t = RunningTimer(
        id = "a",
        name = "Cuisson",
        durationSeconds = 180,
        startedAtMillis = 0L,
    )

    @Test fun remainingSeconds_counts_down_to_zero() {
        assertEquals(180, t.remainingSeconds(0))
        assertEquals(120, t.remainingSeconds(60_000))
        assertEquals(0, t.remainingSeconds(180_000))
        assertTrue(t.remainingSeconds(200_000) < 0)
    }

    @Test fun isExpired_turns_true_at_zero() {
        assertFalse(t.isExpired(179_000))
        assertTrue(t.isExpired(180_000))
    }

    @Test fun formatRemaining_renders_MM_SS() {
        assertEquals("3:00", formatRemaining(180))
        assertEquals("2:05", formatRemaining(125))
        assertEquals("0:59", formatRemaining(59))
        assertEquals("0:00", formatRemaining(-5))
    }
}
