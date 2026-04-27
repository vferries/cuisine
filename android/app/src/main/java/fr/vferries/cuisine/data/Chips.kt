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
    ChipKey.DESSERT to { r -> "dessert" in r.tags },
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
