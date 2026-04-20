package fr.vferries.cuisine.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipesIndexTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parses_a_minimal_index_with_one_recipe() {
        val raw = """
            {
              "version": "2026-04-20",
              "generatedAt": "2026-04-20",
              "recipes": [
                {
                  "slug": "porc-bigorre-caramel",
                  "title": "Porc noir de Bigorre",
                  "servings": 2,
                  "prepTime": 20,
                  "cookTime": 25,
                  "totalTime": 45,
                  "difficulty": "moyenne",
                  "cuisine": "vietnamienne",
                  "tags": ["porc", "asiatique"],
                  "ingredientNames": ["sucre"],
                  "updatedAt": "2026-04-20T00:00:00.000Z"
                }
              ]
            }
        """.trimIndent()

        val index = json.decodeFromString<RecipesIndex>(raw)

        assertEquals(1, index.recipes.size)
        assertEquals("porc-bigorre-caramel", index.recipes[0].slug)
        assertEquals("Porc noir de Bigorre", index.recipes[0].title)
        assertEquals(listOf("porc", "asiatique"), index.recipes[0].tags)
    }
}
