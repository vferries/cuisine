package fr.vferries.cuisine.data

import fr.vferries.cuisine.data.cache.RecipeDao
import fr.vferries.cuisine.data.cache.RecipeEntity
import fr.vferries.cuisine.data.cache.toDomain
import fr.vferries.cuisine.data.cache.toEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Wraps a remote [network] source avec un cache persistant via [dao].
 * - `listRecipes()` : essaie le réseau, upsert le cache et retourne. Sur erreur,
 *   retourne le cache (même vide).
 * - `getRecipe(slug)` : idem, avec deserialize du JSON caché sur fallback.
 */
class CachedRecipeRepository(
    private val network: RecipeRepository,
    private val dao: RecipeDao,
) : RecipeRepository {

    private val json = Json { ignoreUnknownKeys = true; classDiscriminator = "type" }

    override suspend fun listRecipes(): List<RecipeMeta> {
        return try {
            val fresh = network.listRecipes()
            dao.upsertAll(fresh.map { it.toEntity() })
            fresh
        } catch (e: Exception) {
            dao.allMeta().map { it.toDomain() }
        }
    }

    override suspend fun getRecipe(slug: String): Recipe {
        return try {
            val fresh = network.getRecipe(slug)
            dao.upsertRecipe(
                RecipeEntity(
                    slug = slug,
                    json = json.encodeToString<Recipe>(fresh),
                    updatedAt = fresh.updatedAt,
                ),
            )
            fresh
        } catch (e: Exception) {
            val cached = dao.recipeBySlug(slug)
                ?: throw e
            json.decodeFromString<Recipe>(cached.json)
        }
    }
}
