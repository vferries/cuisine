package fr.vferries.cuisine.data

interface RecipeRepository {
    suspend fun listRecipes(): List<RecipeMeta>
}
