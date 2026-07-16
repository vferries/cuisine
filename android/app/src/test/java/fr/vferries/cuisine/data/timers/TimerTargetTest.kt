package fr.vferries.cuisine.data.timers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimerTargetTest {

    @Test fun parses_a_wellformed_id() {
        assertEquals(
            TimerTarget("sukiyaki-udon", 1, 2, 3),
            parseTimerId("sukiyaki-udon:1:2:3"),
        )
    }

    @Test fun rejects_wrong_segment_count() {
        assertNull(parseTimerId(""))
        assertNull(parseTimerId("slug:1:2"))
        assertNull(parseTimerId("slug:1:2:3:4"))
    }

    @Test fun rejects_non_numeric_indices() {
        assertNull(parseTimerId("slug:a:2:3"))
        assertNull(parseTimerId("slug:1:b:3"))
        assertNull(parseTimerId("slug:1:2:c"))
    }

    @Test fun rejects_blank_slug() {
        assertNull(parseTimerId(":1:2:3"))
    }
}
