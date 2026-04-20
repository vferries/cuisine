package fr.vferries.cuisine.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import fr.vferries.cuisine.data.cache.CuisineDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CachedRecipeRepositoryTest {

    private lateinit var db: CuisineDatabase

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, CuisineDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After fun tearDown() = db.close()

    private fun meta(slug: String, title: String) = RecipeMeta(
        slug = slug,
        title = title,
        servings = 2,
        prepTime = 10,
        cookTime = 20,
        totalTime = 30,
        difficulty = "moyenne",
        cuisine = "test",
        updatedAt = "2026-04-20",
    )

    private class ScriptedNetwork(
        var listResult: Result<List<RecipeMeta>>,
        var recipeResult: Result<Recipe> = Result.failure(IllegalStateException("no recipe scripted")),
    ) : RecipeRepository {
        override suspend fun listRecipes() = listResult.getOrThrow()
        override suspend fun getRecipe(slug: String) = recipeResult.getOrThrow()
    }

    @Test
    fun returns_fresh_and_updates_cache_on_network_success() = runTest {
        val network = ScriptedNetwork(Result.success(listOf(meta("a", "A"), meta("b", "B"))))
        val repo = CachedRecipeRepository(network, db.recipeDao())

        val result = repo.listRecipes()

        assertEquals(2, result.size)
        assertEquals(2, db.recipeDao().allMeta().size)
    }

    @Test
    fun falls_back_to_cache_on_network_failure() = runTest {
        // Prime cache avec un premier appel réussi
        val network = ScriptedNetwork(Result.success(listOf(meta("a", "A"))))
        val repo = CachedRecipeRepository(network, db.recipeDao())
        repo.listRecipes()

        // Seconde tentative : le réseau échoue, on relit le cache
        network.listResult = Result.failure(RuntimeException("offline"))
        val result = repo.listRecipes()

        assertEquals(1, result.size)
        assertEquals("A", result[0].title)
    }

    @Test
    fun getRecipe_falls_back_to_cache() = runTest {
        val recipe = Recipe(slug = "porc", metadata = mapOf("title" to "Porc"), updatedAt = "now")
        val network = ScriptedNetwork(
            listResult = Result.success(emptyList()),
            recipeResult = Result.success(recipe),
        )
        val repo = CachedRecipeRepository(network, db.recipeDao())
        repo.getRecipe("porc") // prime cache

        network.recipeResult = Result.failure(RuntimeException("offline"))
        val offline = repo.getRecipe("porc")

        assertEquals("porc", offline.slug)
        assertEquals("Porc", offline.metadata["title"])
    }

    @Test
    fun getRecipe_rethrows_when_no_cache() = runTest {
        val network = ScriptedNetwork(
            listResult = Result.success(emptyList()),
            recipeResult = Result.failure(RuntimeException("404")),
        )
        val repo = CachedRecipeRepository(network, db.recipeDao())

        val error = runCatching { repo.getRecipe("inconnu") }.exceptionOrNull()
        assertTrue(error is RuntimeException)
    }
}
