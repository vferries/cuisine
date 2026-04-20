package fr.vferries.cuisine.data.cache

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RecipeDaoTest {

    private lateinit var db: CuisineDatabase
    private lateinit var dao: RecipeDao

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, CuisineDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.recipeDao()
    }

    @After fun tearDown() = db.close()

    private fun entity(slug: String, updated: String = "2026-04-20") = RecipeMetaEntity(
        slug = slug,
        title = "Title $slug",
        source = null,
        servings = 2,
        prepTime = 10,
        cookTime = 20,
        totalTime = 30,
        difficulty = "moyenne",
        cuisine = "test",
        region = null,
        tags = listOf("a", "b"),
        image = null,
        ingredientNames = listOf("sucre"),
        updatedAt = updated,
    )

    @Test
    fun upsert_then_read_roundtrip() = runTest {
        dao.upsertAll(listOf(entity("a"), entity("b")))
        val all = dao.allMeta()
        assertEquals(2, all.size)
        assertEquals(listOf("a", "b"), all.map { it.slug }.sorted())
        assertEquals(listOf("a", "b"), all.first { it.slug == "a" }.tags)
    }

    @Test
    fun upsert_replaces_existing_by_primary_key() = runTest {
        dao.upsertAll(listOf(entity("a", updated = "2026-01-01")))
        dao.upsertAll(listOf(entity("a", updated = "2026-04-20")))
        assertEquals(1, dao.allMeta().size)
        assertEquals("2026-04-20", dao.allMeta().first().updatedAt)
    }

    @Test
    fun recipe_full_roundtrip() = runTest {
        dao.upsertRecipe(RecipeEntity(slug = "x", json = "{\"slug\":\"x\"}", updatedAt = "now"))
        val cached = dao.recipeBySlug("x")
        assertEquals("{\"slug\":\"x\"}", cached?.json)
        assertNull(dao.recipeBySlug("nope"))
    }
}
