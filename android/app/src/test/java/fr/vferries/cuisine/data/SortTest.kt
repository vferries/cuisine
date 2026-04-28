package fr.vferries.cuisine.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SortTest {

    private fun meta(
        slug: String,
        title: String,
        totalTime: Int,
        updatedAt: String,
    ) = RecipeMeta(
        slug = slug,
        title = title,
        servings = 2,
        prepTime = 0,
        cookTime = 0,
        totalTime = totalTime,
        difficulty = "moyenne",
        cuisine = "test",
        updatedAt = updatedAt,
    )

    private val a = meta("a", "Bouillon de châtaigne", 40, "2026-04-10T00:00:00.000Z")
    private val b = meta("b", "Flan caramel", 80, "2026-04-20T00:00:00.000Z")
    private val c = meta("c", "Aubergine miso", 25, "2026-04-15T00:00:00.000Z")

    @Test fun recent_sorts_by_updatedAt_descending() {
        assertEquals(
            listOf("b", "c", "a"),
            sortRecipes(listOf(a, b, c), SortMode.RECENT),
        )
    }

    @Test fun alpha_sorts_by_title_ascending_french() {
        assertEquals(
            listOf("c", "a", "b"),
            sortRecipes(listOf(a, b, c), SortMode.ALPHA),
        )
    }

    @Test fun duration_sorts_by_totalTime_ascending() {
        assertEquals(
            listOf("c", "a", "b"),
            sortRecipes(listOf(a, b, c), SortMode.DURATION),
        )
    }
}
