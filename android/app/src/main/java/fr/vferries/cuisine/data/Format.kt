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
 * Scale une qty texte : multiplie par [ratio] si parseable en nombre, renvoie
 * telle quelle sinon (ex. "au goût" → "au goût"). Les entiers restent entiers,
 * sinon arrondi à 2 décimales.
 */
fun scaleQuantityText(qty: String, ratio: Double): String {
    val n = qty.toDoubleOrNull() ?: return qty
    val scaled = n * ratio
    val rounded = Math.round(scaled * 100).toDouble() / 100.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString()
    else rounded.toString()
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
