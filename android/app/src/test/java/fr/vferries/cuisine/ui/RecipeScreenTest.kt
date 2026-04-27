package fr.vferries.cuisine.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import fr.vferries.cuisine.data.favorites.FavoritesStore
import org.junit.Before
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

    private val recipe = Recipe(
        slug = "porc",
        metadata = mapOf("title" to "Porc noir", "cuisine" to "vietnamienne", "servings" to "2"),
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

    @Test
    fun title_and_ingredients_tab_are_default() {
        composeRule.setContent { RecipeScreen(state = RecipeState.Success(recipe)) }
        composeRule.onNodeWithText("Porc noir").assertIsDisplayed()
        composeRule.onNodeWithText("porc").assertIsDisplayed()
        composeRule.onNodeWithText("sucre").assertIsDisplayed()
    }

    @Test
    fun steps_tab_reveals_section_and_step_text() {
        composeRule.setContent { RecipeScreen(state = RecipeState.Success(recipe)) }
        composeRule.onNodeWithText("Étapes").performClick()
        composeRule.onNodeWithText("Préparation").assertIsDisplayed()
        // Chaque token du step est un nœud séparé (FlowRow).
        composeRule.onNodeWithText("Découper le ").assertIsDisplayed()
        composeRule.onNodeWithText(" en cubes.").assertIsDisplayed()
    }

    @Test
    fun loading_state_shows_loading_label() {
        composeRule.setContent { RecipeScreen(state = RecipeState.Loading) }
        composeRule.onNodeWithText("Chargement…").assertIsDisplayed()
    }

    @Before
    fun resetFavorites() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        FavoritesStore.from(ctx).clear()
    }

    @Test
    fun favorite_button_toggles_label_and_persists() {
        composeRule.setContent { RecipeScreen(state = RecipeState.Success(recipe)) }
        composeRule.onNodeWithContentDescription("Marquer comme favori").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Marquer comme favori").performClick()
        composeRule.onNodeWithContentDescription("Retirer des favoris").assertIsDisplayed()

        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        org.junit.Assert.assertTrue(FavoritesStore.from(ctx).contains("porc"))
    }
}
