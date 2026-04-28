package fr.vferries.cuisine.data

enum class SearchScope { ALL, INGREDIENTS }

/**
 * Matching substring insensible à la casse. Scope ALL = titre + cuisine
 * + tags + ingrédients. Scope INGREDIENTS = ingrédients uniquement.
 * Requête vide → tous les slugs.
 */
fun matchingRecipeSlugs(
    recipes: List<RecipeMeta>,
    query: String,
    scope: SearchScope = SearchScope.ALL,
): List<String> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return recipes.map { it.slug }
    return recipes.filter { r ->
        when (scope) {
            SearchScope.INGREDIENTS ->
                r.ingredientNames.any { it.contains(q, ignoreCase = true) }
            SearchScope.ALL ->
                r.title.contains(q, ignoreCase = true)
                    || r.cuisine.contains(q, ignoreCase = true)
                    || r.tags.any { it.contains(q, ignoreCase = true) }
                    || r.ingredientNames.any { it.contains(q, ignoreCase = true) }
        }
    }.map { it.slug }
}
