package fr.vferries.cuisine.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    @Test
    fun parses_a_minimal_recipe_with_mixed_quantity_shapes() {
        val raw = """
            {
              "slug": "test",
              "metadata": { "title": "Test", "servings": "2" },
              "sections": [
                {
                  "name": "Préparation",
                  "steps": [
                    {
                      "tokens": [
                        { "type": "text", "text": "Mélanger " },
                        { "type": "ingredient", "ingredient": { "name": "sucre", "quantity": 6, "unit": "càc" } },
                        { "type": "text", "text": " avec " },
                        { "type": "ingredient", "ingredient": { "name": "eau" } }
                      ]
                    }
                  ]
                }
              ],
              "tips": ["Ajoutez une pincée de patience."],
              "ingredients": [
                { "name": "sucre", "quantity": 6, "unit": "càc" },
                { "name": "eau" }
              ],
              "cookware": [{ "name": "bol", "quantity": 1 }],
              "timers": [],
              "updatedAt": "2026-04-20T00:00:00.000Z"
            }
        """.trimIndent()

        val recipe = json.decodeFromString<Recipe>(raw)

        assertEquals("test", recipe.slug)
        assertEquals("Test", recipe.metadata["title"])
        assertEquals(1, recipe.sections.size)
        assertEquals("Préparation", recipe.sections[0].name)

        val tokens = recipe.sections[0].steps[0].tokens
        assertEquals(4, tokens.size)
        assertTrue(tokens[0] is StepToken.Text)
        assertTrue(tokens[1] is StepToken.IngredientToken)
        assertEquals("6", (tokens[1] as StepToken.IngredientToken).ingredient.quantity)

        assertEquals("6", recipe.ingredients[0].quantity)
        assertEquals("", recipe.ingredients[1].quantity) // absent → chaîne vide
    }
}
