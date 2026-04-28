package fr.vferries.cuisine.data

import java.text.Collator
import java.util.Locale

enum class SortMode(val label: String) {
    RECENT("Récent"),
    ALPHA("Alphabétique"),
    DURATION("Durée"),
}

fun sortRecipes(recipes: List<RecipeMeta>, mode: SortMode): List<String> {
    return when (mode) {
        SortMode.RECENT -> recipes.sortedByDescending { it.updatedAt }
        SortMode.ALPHA -> {
            val collator = Collator.getInstance(Locale.FRENCH)
            recipes.sortedWith(compareBy(collator) { it.title })
        }
        SortMode.DURATION -> recipes.sortedBy { it.totalTime }
    }.map { it.slug }
}
