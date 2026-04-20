package fr.vferries.cuisine.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import fr.vferries.cuisine.data.RecipeMeta
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun meta(slug: String, title: String) = RecipeMeta(
        slug = slug,
        title = title,
        servings = 2,
        prepTime = 10,
        cookTime = 20,
        totalTime = 30,
        difficulty = "moyenne",
        cuisine = "test",
        updatedAt = "2026-04-20T00:00:00.000Z",
    )

    @Test
    fun success_state_displays_all_titles() {
        val recipes = listOf(
            meta("a", "Recette A"),
            meta("b", "Recette B"),
        )
        composeRule.setContent {
            HomeScreen(state = HomeState.Success(recipes))
        }
        composeRule.onNodeWithText("Recette A").assertIsDisplayed()
        composeRule.onNodeWithText("Recette B").assertIsDisplayed()
    }

    @Test
    fun loading_state_displays_loading_label() {
        composeRule.setContent {
            HomeScreen(state = HomeState.Loading)
        }
        composeRule.onNodeWithText("Chargement…").assertIsDisplayed()
    }

    @Test
    fun error_state_displays_error_message() {
        composeRule.setContent {
            HomeScreen(state = HomeState.Error("Oups"))
        }
        composeRule.onNodeWithText("Erreur : Oups").assertIsDisplayed()
    }
}
