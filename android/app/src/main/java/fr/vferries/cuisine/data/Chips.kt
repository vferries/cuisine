package fr.vferries.cuisine.data

enum class ChipKey(val label: String) {
    ALL("Toutes"),
    FAVORIS("Favoris"),
    RAPIDE("Rapide"),
    VEGE("Végé"),
    ASIATIQUE("Asiatique"),
    FRANCAIS("Français"),
    DESSERT("Dessert"),
}

private val predicates: Map<ChipKey, (RecipeMeta) -> Boolean> = mapOf(
    ChipKey.ALL to { _ -> true },
    ChipKey.RAPIDE to { r -> r.totalTime <= 30 },
    ChipKey.VEGE to { r -> "végé" in r.tags },
    ChipKey.ASIATIQUE to { r -> "asiatique" in r.tags },
    ChipKey.FRANCAIS to { r -> r.cuisine == "française" },
    ChipKey.DESSERT to { r -> r.course == "dessert" || "dessert" in r.tags },
)

fun filterByChip(
    recipes: List<RecipeMeta>,
    chip: ChipKey,
    favorites: Set<String> = emptySet(),
): List<String> {
    if (chip == ChipKey.FAVORIS) {
        return recipes.filter { it.slug in favorites }.map { it.slug }
    }
    val predicate = predicates.getValue(chip)
    return recipes.filter(predicate).map { it.slug }
}

enum class Course(val label: String, val value: String) {
    ENTREE("Entrée", "entrée"),
    PLAT("Plat", "plat"),
    DESSERT("Dessert", "dessert");
}

fun filterByCourse(recipes: List<RecipeMeta>, course: Course?): List<String> {
    if (course == null) return recipes.map { it.slug }
    return recipes.filter { it.course == course.value }.map { it.slug }
}

enum class Difficulty(val label: String, val value: String) {
    FACILE("Facile", "facile"),
    MOYENNE("Moyenne", "moyenne"),
    DIFFICILE("Difficile", "difficile");
}

fun filterByDifficulty(
    recipes: List<RecipeMeta>,
    difficulty: Difficulty?,
): List<String> {
    if (difficulty == null) return recipes.map { it.slug }
    return recipes.filter { it.difficulty == difficulty.value }.map { it.slug }
}

fun filterBySansGluten(recipes: List<RecipeMeta>, active: Boolean): List<String> {
    if (!active) return recipes.map { it.slug }
    return recipes.filter { "sans gluten" in it.tags }.map { it.slug }
}
