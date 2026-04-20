package fr.vferries.cuisine.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URL

class HttpRecipeRepository(
    private val baseUrl: String = "https://vferries.github.io/cuisine",
) : RecipeRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    override suspend fun listRecipes(): List<RecipeMeta> = withContext(Dispatchers.IO) {
        val raw = URL("$baseUrl/index.json").readText(Charsets.UTF_8)
        json.decodeFromString<RecipesIndex>(raw).recipes
    }

    override suspend fun getRecipe(slug: String): Recipe = withContext(Dispatchers.IO) {
        val raw = URL("$baseUrl/recipes/$slug.json").readText(Charsets.UTF_8)
        json.decodeFromString<Recipe>(raw)
    }
}
