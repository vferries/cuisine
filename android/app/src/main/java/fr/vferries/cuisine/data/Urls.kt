package fr.vferries.cuisine.data

object Urls {
    const val BASE = "https://vferries.github.io/cuisine"

    fun thumbUrl(slug: String): String = "$BASE/images/$slug.thumb.webp"
    fun heroUrl(slug: String): String = "$BASE/images/$slug.webp"
    fun recipeUrl(slug: String): String = "$BASE/$slug"
}
