package fr.vferries.cuisine.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ChipsTest {

    private fun meta(
        slug: String,
        cuisine: String = "vietnamienne",
        tags: List<String> = emptyList(),
        totalTime: Int = 30,
    ) = RecipeMeta(
        slug = slug,
        title = "t",
        servings = 2,
        prepTime = 10,
        cookTime = 20,
        totalTime = totalTime,
        difficulty = "moyenne",
        cuisine = cuisine,
        tags = tags,
        updatedAt = "2026-04-20",
    )

    private val porc = meta("porc", totalTime = 45, tags = listOf("asiatique"))
    private val salade = meta("salade", cuisine = "italienne", tags = listOf("végé"), totalTime = 10)
    private val tarte = meta("tarte", cuisine = "française", tags = listOf("dessert"), totalTime = 60)

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
}
