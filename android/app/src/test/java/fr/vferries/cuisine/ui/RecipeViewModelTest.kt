package fr.vferries.cuisine.ui

import fr.vferries.cuisine.data.Recipe
import fr.vferries.cuisine.data.RecipeMeta
import fr.vferries.cuisine.data.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setMainDispatcher() = Dispatchers.setMain(dispatcher)
    @After fun resetMainDispatcher() = Dispatchers.resetMain()

    private class FakeRepo(private val recipe: Recipe) : RecipeRepository {
        override suspend fun listRecipes(): List<RecipeMeta> = emptyList()
        override suspend fun getRecipe(slug: String): Recipe = recipe.copy(slug = slug)
    }

    private class FailingRepo(val error: Throwable) : RecipeRepository {
        override suspend fun listRecipes(): List<RecipeMeta> = throw error
        override suspend fun getRecipe(slug: String): Recipe = throw error
    }

    @Test
    fun state_becomes_Success_with_recipe_from_repo() = runTest(dispatcher) {
        val recipe = Recipe(
            slug = "x",
            metadata = mapOf("title" to "Porc"),
            updatedAt = "now",
        )
        val vm = RecipeViewModel(FakeRepo(recipe), slug = "porc-bigorre")
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is RecipeState.Success)
        assertEquals("porc-bigorre", (state as RecipeState.Success).recipe.slug)
        assertEquals("Porc", state.recipe.metadata["title"])
    }

    @Test
    fun state_becomes_Error_when_repo_throws() = runTest(dispatcher) {
        val vm = RecipeViewModel(FailingRepo(RuntimeException("404")), slug = "any")
        advanceUntilIdle()
        assertTrue(vm.state.value is RecipeState.Error)
    }
}
