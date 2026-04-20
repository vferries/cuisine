package fr.vferries.cuisine.data

import kotlinx.serialization.Serializable

@Serializable
data class RecipeMeta(
    val slug: String,
    val title: String,
    val source: String? = null,
    val servings: Int,
    val prepTime: Int,
    val cookTime: Int,
    val totalTime: Int,
    val difficulty: String,
    val cuisine: String,
    val region: String? = null,
    val tags: List<String> = emptyList(),
    val image: String? = null,
    val ingredientNames: List<String> = emptyList(),
    val updatedAt: String,
)

@Serializable
data class RecipesIndex(
    val version: String,
    val generatedAt: String,
    val recipes: List<RecipeMeta>,
)
