package fr.vferries.cuisine.data

/**
 * Matching substring insensible à la casse sur titre + cuisine + tags +
 * ingrédients. Requête vide → tous les slugs. Suffit pour une centaine
 * de recettes ; on passera sur un index scoré (type MiniSearch) si besoin.
 */
fun matchingRecipeSlugs(recipes: List<RecipeMeta>, query: String): List<String> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return recipes.map { it.slug }
    return recipes.filter { r ->
        r.title.contains(q, ignoreCase = true)
            || r.cuisine.contains(q, ignoreCase = true)
            || r.tags.any { it.contains(q, ignoreCase = true) }
            || r.ingredientNames.any { it.contains(q, ignoreCase = true) }
    }.map { it.slug }
}
