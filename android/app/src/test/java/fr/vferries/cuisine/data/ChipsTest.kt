package fr.vferries.cuisine.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ChipsTest {

    private fun meta(
        slug: String,
        cuisine: String = "vietnamienne",
        course: String = "plat",
        difficulty: String = "moyenne",
        tags: List<String> = emptyList(),
        totalTime: Int = 30,
    ) = RecipeMeta(
        slug = slug,
        title = "t",
        servings = 2,
        prepTime = 10,
        cookTime = 20,
        totalTime = totalTime,
        difficulty = difficulty,
        cuisine = cuisine,
        course = course,
        tags = tags,
        updatedAt = "2026-04-20",
    )

    private val porc = meta("porc", totalTime = 45, tags = listOf("asiatique"))
    private val salade = meta("salade", cuisine = "italienne", course = "entrée", difficulty = "facile", tags = listOf("végé"), totalTime = 10)
    private val tarte = meta("tarte", cuisine = "française", course = "dessert", difficulty = "difficile", tags = listOf("dessert", "sans gluten"), totalTime = 60)

    @Test fun all_returns_all_slugs() {
        assertEquals(
            listOf("porc", "salade", "tarte"),
            filterByChip(listOf(porc, salade, tarte), ChipKey.ALL),
        )
    }

    @Test fun rapide_filters_on_totalTime_le_30() {
        assertEquals(listOf("salade"), filterByChip(listOf(porc, salade, tarte), ChipKey.RAPIDE))
    }

    @Test fun vege_filters_on_tag() {
        assertEquals(listOf("salade"), filterByChip(listOf(porc, salade, tarte), ChipKey.VEGE))
    }

    @Test fun asiatique_filters_on_tag() {
        assertEquals(listOf("porc"), filterByChip(listOf(porc, salade, tarte), ChipKey.ASIATIQUE))
    }

    @Test fun francais_filters_on_cuisine() {
        assertEquals(listOf("tarte"), filterByChip(listOf(porc, salade, tarte), ChipKey.FRANCAIS))
    }

    @Test fun dessert_filters_on_tag() {
        assertEquals(listOf("tarte"), filterByChip(listOf(porc, salade, tarte), ChipKey.DESSERT))
    }

    @Test fun favoris_filters_on_provided_set() {
        assertEquals(
            listOf("porc", "tarte"),
            filterByChip(
                listOf(porc, salade, tarte),
                ChipKey.FAVORIS,
                favorites = setOf("porc", "tarte"),
            ),
        )
    }

    @Test fun favoris_returns_empty_when_no_favorites() {
        assertEquals(
            emptyList<String>(),
            filterByChip(
                listOf(porc, salade, tarte),
                ChipKey.FAVORIS,
                favorites = emptySet(),
            ),
        )
    }

    @Test fun course_null_returns_all() {
        assertEquals(
            listOf("porc", "salade", "tarte"),
            filterByCourse(listOf(porc, salade, tarte), null),
        )
    }

    @Test fun course_entree_keeps_only_entrees() {
        assertEquals(
            listOf("salade"),
            filterByCourse(listOf(porc, salade, tarte), Course.ENTREE),
        )
    }

    @Test fun course_dessert_keeps_only_desserts() {
        assertEquals(
            listOf("tarte"),
            filterByCourse(listOf(porc, salade, tarte), Course.DESSERT),
        )
    }

    @Test fun difficulty_facile_keeps_only_faciles() {
        assertEquals(
            listOf("salade"),
            filterByDifficulty(listOf(porc, salade, tarte), Difficulty.FACILE),
        )
    }

    @Test fun sans_gluten_inactive_returns_all() {
        assertEquals(
            listOf("porc", "salade", "tarte"),
            filterBySansGluten(listOf(porc, salade, tarte), false),
        )
    }

    @Test fun sans_gluten_active_keeps_only_tagged() {
        assertEquals(
            listOf("tarte"),
            filterBySansGluten(listOf(porc, salade, tarte), true),
        )
    }
}
