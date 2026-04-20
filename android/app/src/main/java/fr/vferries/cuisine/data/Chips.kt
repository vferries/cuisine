package fr.vferries.cuisine.data

enum class ChipKey(val label: String) {
    ALL("Toutes"),
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

fun filterByChip(recipes: List<RecipeMeta>, chip: ChipKey): List<String> {
    val predicate = predicates.getValue(chip)
    return recipes.filter(predicate).map { it.slug }
}
