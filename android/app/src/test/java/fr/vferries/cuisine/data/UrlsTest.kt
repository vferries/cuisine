package fr.vferries.cuisine.data

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlsTest {
    @Test
    fun thumb_and_hero_point_to_pages() {
        assertEquals(
            "https://vferries.github.io/cuisine/images/porc.thumb.webp",
            Urls.thumbUrl("porc"),
        )
        assertEquals(
            "https://vferries.github.io/cuisine/images/porc.webp",
            Urls.heroUrl("porc"),
        )
    }
}
