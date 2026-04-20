package fr.vferries.cuisine.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import fr.vferries.cuisine.data.Ingredient
import fr.vferries.cuisine.data.Recipe
import fr.vferries.cuisine.data.Section
import fr.vferries.cuisine.data.Step
import fr.vferries.cuisine.data.StepToken
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RecipeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun success_state_shows_title_ingredients_and_step_text() {
        val recipe = Recipe(
            slug = "porc",
            metadata = mapOf("title" to "Porc noir", "cuisine" to "vietnamienne"),
            sections = listOf(
                Section(
                    name = "Préparation",
                    steps = listOf(
                        Step(
                            tokens = listOf(
                                StepToken.Text("Découper le "),
                                StepToken.IngredientToken(Ingredient("porc")),
                                StepToken.Text(" en cubes."),
                            ),
                        ),
                    ),
                ),
            ),
            ingredients = listOf(
                Ingredient("porc", quantity = "500", unit = "g"),
                Ingredient("sucre", quantity = "6", unit = "càc"),
            ),
            updatedAt = "now",
        )

        composeRule.setContent { RecipeScreen(state = RecipeState.Success(recipe)) }

        composeRule.onNodeWithText("Porc noir").assertIsDisplayed()
        composeRule.onNodeWithText("Préparation").assertIsDisplayed()
        composeRule.onNodeWithText("Découper le porc en cubes.").assertIsDisplayed()
        composeRule.onNodeWithText("porc").assertIsDisplayed()
        composeRule.onNodeWithText("sucre").assertIsDisplayed()
    }

    @Test
    fun loading_state_shows_loading_label() {
        composeRule.setContent { RecipeScreen(state = RecipeState.Loading) }
        composeRule.onNodeWithText("Chargement…").assertIsDisplayed()
    }
}
