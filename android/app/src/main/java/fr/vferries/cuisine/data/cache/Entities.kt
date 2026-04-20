package fr.vferries.cuisine.data.cache

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import fr.vferries.cuisine.data.RecipeMeta
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "recipe_meta")
data class RecipeMetaEntity(
    @PrimaryKey val slug: String,
    val title: String,
    val source: String?,
    val servings: Int,
    val prepTime: Int,
    val cookTime: Int,
    val totalTime: Int,
    val difficulty: String,
    val cuisine: String,
    val region: String?,
    val tags: List<String>,
    val image: String?,
    val ingredientNames: List<String>,
    val updatedAt: String,
)

fun RecipeMeta.toEntity() = RecipeMetaEntity(
    slug = slug,
    title = title,
    source = source,
    servings = servings,
    prepTime = prepTime,
    cookTime = cookTime,
    totalTime = totalTime,
    difficulty = difficulty,
    cuisine = cuisine,
    region = region,
    tags = tags,
    image = image,
    ingredientNames = ingredientNames,
    updatedAt = updatedAt,
)

fun RecipeMetaEntity.toDomain() = RecipeMeta(
    slug = slug,
    title = title,
    source = source,
    servings = servings,
    prepTime = prepTime,
    cookTime = cookTime,
    totalTime = totalTime,
    difficulty = difficulty,
    cuisine = cuisine,
    region = region,
    tags = tags,
    image = image,
    ingredientNames = ingredientNames,
    updatedAt = updatedAt,
)

@Entity(tableName = "recipe_full")
data class RecipeEntity(
    @PrimaryKey val slug: String,
    val json: String,
    val updatedAt: String,
)

class Converters {
    private val jsonFmt = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun stringListToJson(value: List<String>): String =
        jsonFmt.encodeToString<List<String>>(value)

    @TypeConverter
    fun jsonToStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else jsonFmt.decodeFromString<List<String>>(value)
}
