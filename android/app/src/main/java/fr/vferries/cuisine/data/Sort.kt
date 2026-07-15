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
        // Tie-break par slug : les recettes committées ensemble partagent
        // la même date git, l'ordre doit rester déterministe.
        SortMode.RECENT -> recipes.sortedWith(
            compareByDescending<RecipeMeta> { it.updatedAt }.thenBy { it.slug },
        )
        SortMode.ALPHA -> {
            val collator = Collator.getInstance(Locale.FRENCH)
            recipes.sortedWith(compareBy(collator) { it.title })
        }
        SortMode.DURATION -> recipes.sortedWith(
            compareBy<RecipeMeta> { it.totalTime }.thenBy { it.slug },
        )
    }.map { it.slug }
}
