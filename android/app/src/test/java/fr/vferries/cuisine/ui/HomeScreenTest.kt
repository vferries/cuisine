package fr.vferries.cuisine.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import fr.vferries.cuisine.data.RecipeMeta
import fr.vferries.cuisine.data.favorites.FavoritesStore
import org.junit.Before
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

    @Before
    fun resetFavorites() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        FavoritesStore.from(ctx).clear()
    }

    @Test
    fun favoris_chip_filters_to_favorited_recipes_only() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        FavoritesStore.from(ctx).toggle("a")

        val recipes = listOf(meta("a", "Recette A"), meta("b", "Recette B"))
        composeRule.setContent { HomeScreen(state = HomeState.Success(recipes)) }

        composeRule.onNodeWithText("Recette A").assertIsDisplayed()
        composeRule.onNodeWithText("Recette B").assertIsDisplayed()

        composeRule.onNodeWithText("Favoris").performClick()

        composeRule.onNodeWithText("Recette A").assertIsDisplayed()
        composeRule.onNodeWithText("Recette B").assertIsNotDisplayed()
    }

    @Test
    fun row_heart_toggles_favorite_state() {
        val recipes = listOf(meta("a", "Recette A"))
        composeRule.setContent { HomeScreen(state = HomeState.Success(recipes)) }

        composeRule.onAllNodesWithContentDescription("Marquer Recette A comme favori")[0].performClick()

        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        org.junit.Assert.assertTrue(FavoritesStore.from(ctx).contains("a"))
    }

    @Test
    fun advanced_panel_toggle_reveals_course_filter() {
        composeRule.setContent { HomeScreen(state = HomeState.Success(emptyList())) }

        composeRule.onNodeWithText("Entrée").assertDoesNotExist()
        composeRule.onNodeWithText("Plat").assertDoesNotExist()
        composeRule.onNodeWithText("Tri").assertDoesNotExist()

        composeRule.onNodeWithText("Filtres avancés").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Entrée").assertExists()
        composeRule.onNodeWithText("Plat").assertExists()
        composeRule.onNodeWithText("Récent").assertExists()
        composeRule.onNodeWithText("Sans gluten").assertExists()
    }
}
