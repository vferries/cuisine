package fr.vferries.cuisine.data

private val UNIT_DISPLAY = mapOf(
    "càc" to "c. à c.",
    "càs" to "c. à s.",
)

private val PLURALIZABLE_UNITS = setOf(
    "brin",
    "sachet",
    "bouquet",
    "gousse",
    "pincée",
)

/** Returns unit display form, pluralized si qty > 1 et l'unité l'accepte. */
fun formatUnit(qty: String, unit: String?): String {
    if (unit.isNullOrEmpty()) return ""
    val display = UNIT_DISPLAY[unit] ?: unit
    val n = qty.toDoubleOrNull()
    return if (n != null && n > 1 && unit in PLURALIZABLE_UNITS) "${display}s" else display
}

/** Returns "<qty> <unit>" (ou "<qty>" ou null si qty vide). */
fun formatQty(qty: String, unit: String?): String? {
    if (qty.isEmpty()) return null
    val u = formatUnit(qty, unit)
    return if (u.isEmpty()) qty else "$qty $u"
}

/**
 * Pluralise le premier mot d'un nom si qty > 1 ; laisse invariant si le mot
 * termine déjà par s/x/z. Supporte les noms composés ("planche à découper"
 * → "planches à découper").
 */
fun pluralizeName(qty: Int, name: String): String {
    if (qty <= 1) return name
    val parts = name.split(" ")
    val first = parts.first()
    if (first.isEmpty() || first.last().lowercaseChar() in setOf('s', 'x', 'z')) return name
    return (listOf("${first}s") + parts.drop(1)).joinToString(" ")
}
