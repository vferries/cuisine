package fr.vferries.cuisine.data

import kotlin.math.floor
import kotlin.random.Random

fun <T> pickRandom(
    items: List<T>,
    rng: () -> Double = { Random.Default.nextDouble() },
): T? {
    if (items.isEmpty()) return null
    val index = floor(rng() * items.size).toInt().coerceIn(0, items.size - 1)
    return items[index]
}
