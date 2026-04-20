package fr.vferries.cuisine.data

interface RecipeRepository {
    suspend fun listRecipes(): List<RecipeMeta>
    suspend fun getRecipe(slug: String): Recipe
}
