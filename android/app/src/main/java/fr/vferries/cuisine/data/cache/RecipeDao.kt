package fr.vferries.cuisine.data.cache

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RecipeDao {

    @Upsert
    suspend fun upsertAll(entities: List<RecipeMetaEntity>)

    @Query("SELECT * FROM recipe_meta ORDER BY updatedAt DESC")
    suspend fun allMeta(): List<RecipeMetaEntity>

    @Upsert
    suspend fun upsertRecipe(entity: RecipeEntity)

    @Query("SELECT * FROM recipe_full WHERE slug = :slug LIMIT 1")
    suspend fun recipeBySlug(slug: String): RecipeEntity?
}
