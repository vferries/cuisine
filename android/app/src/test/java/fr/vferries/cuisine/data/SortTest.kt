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

    @Test fun recent_breaks_updatedAt_ties_by_slug() {
        val t = "2026-04-28T07:05:10.000Z"
        val x = meta("z-dernier", "X", 10, t)
        val y = meta("a-premier", "Y", 10, t)
        val z = meta("m-milieu", "Z", 10, t)
        val expected = listOf("a-premier", "m-milieu", "z-dernier")
        assertEquals(expected, sortRecipes(listOf(x, y, z), SortMode.RECENT))
        assertEquals(expected, sortRecipes(listOf(z, x, y), SortMode.RECENT))
    }

    @Test fun duration_breaks_totalTime_ties_by_slug() {
        val x = meta("z-dernier", "X", 30, "2026-04-01T00:00:00.000Z")
        val y = meta("a-premier", "Y", 30, "2026-04-02T00:00:00.000Z")
        val expected = listOf("a-premier", "z-dernier")
        assertEquals(expected, sortRecipes(listOf(x, y), SortMode.DURATION))
        assertEquals(expected, sortRecipes(listOf(y, x), SortMode.DURATION))
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
