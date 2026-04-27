package fr.vferries.cuisine.data.favorites

import android.content.Context
import android.content.SharedPreferences

class FavoritesStore(private val prefs: SharedPreferences) {

    fun get(): Set<String> = prefs.getStringSet(KEY, emptySet()) ?: emptySet()

    fun contains(slug: String): Boolean = slug in get()

    fun toggle(slug: String) {
        val current = get()
        val next = if (slug in current) current - slug else current + slug
        prefs.edit().putStringSet(KEY, next).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    companion object {
        private const val KEY = "favorites"
        fun from(context: Context): FavoritesStore =
            FavoritesStore(
                context.applicationContext.getSharedPreferences(
                    "cuisine",
                    Context.MODE_PRIVATE,
                ),
            )
    }
}
