package fr.vferries.cuisine.ui

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
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setMainDispatcher() = Dispatchers.setMain(dispatcher)
    @After fun resetMainDispatcher() = Dispatchers.resetMain()

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

    private class FakeRepo(val recipes: List<RecipeMeta>) : RecipeRepository {
        override suspend fun listRecipes(): List<RecipeMeta> = recipes
    }

    private class FailingRepo(val error: Throwable) : RecipeRepository {
        override suspend fun listRecipes(): List<RecipeMeta> = throw error
    }

    @Test
    fun state_becomes_Success_after_repository_returns_recipes() = runTest(dispatcher) {
        val repo = FakeRepo(listOf(meta("a", "Recette A"), meta("b", "Recette B")))
        val vm = HomeViewModel(repo)
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is HomeState.Success)
        assertEquals(2, (state as HomeState.Success).recipes.size)
    }

    @Test
    fun state_becomes_Error_when_repository_throws() = runTest(dispatcher) {
        val repo = FailingRepo(RuntimeException("network down"))
        val vm = HomeViewModel(repo)
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is HomeState.Error)
        assertTrue((state as HomeState.Error).message.contains("network down"))
    }
}
