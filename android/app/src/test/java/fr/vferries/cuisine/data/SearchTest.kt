package fr.vferries.cuisine.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchTest {

    private fun meta(
        slug: String,
        title: String,
        cuisine: String = "vietnamienne",
        tags: List<String> = emptyList(),
        ingredients: List<String> = emptyList(),
    ) = RecipeMeta(
        slug = slug,
        title = title,
        servings = 2,
        prepTime = 10,
        cookTime = 20,
        totalTime = 30,
        difficulty = "moyenne",
        cuisine = cuisine,
        tags = tags,
        ingredientNames = ingredients,
        updatedAt = "2026-04-20",
    )

    private val porc = meta(
        "porc-bigorre-caramel",
        "Porc noir de Bigorre",
        tags = listOf("porc", "asiatique"),
        ingredients = listOf("sucre", "piment"),
    )
    private val risotto = meta(
        "risotto",
        "Risotto aux champignons",
        cuisine = "italienne",
        tags = listOf("végé"),
        ingredients = listOf("riz arborio"),
    )

    @Test fun empty_query_returns_all() {
        assertEquals(
            listOf("porc-bigorre-caramel", "risotto"),
            matchingRecipeSlugs(listOf(porc, risotto), ""),
        )
    }

    @Test fun matches_by_title_case_insensitive() {
        assertEquals(listOf("porc-bigorre-caramel"), matchingRecipeSlugs(listOf(porc, risotto), "porc"))
        assertEquals(listOf("porc-bigorre-caramel"), matchingRecipeSlugs(listOf(porc, risotto), "BIGORRE"))
    }

    @Test fun matches_by_cuisine() {
        assertEquals(listOf("risotto"), matchingRecipeSlugs(listOf(porc, risotto), "italienne"))
    }

    @Test fun matches_by_tag() {
        assertEquals(listOf("risotto"), matchingRecipeSlugs(listOf(porc, risotto), "végé"))
    }

    @Test fun matches_by_ingredient() {
        assertEquals(listOf("risotto"), matchingRecipeSlugs(listOf(porc, risotto), "arborio"))
    }

    @Test fun returns_empty_when_nothing_matches() {
        assertEquals(emptyList<String>(), matchingRecipeSlugs(listOf(porc, risotto), "xyzzy"))
    }
}
