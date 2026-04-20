package fr.vferries.cuisine.data.checklist

import android.content.Context
import android.content.SharedPreferences

/**
 * Persistance simple des ingrédients cochés par recette, basée sur
 * SharedPreferences. Clé = "ingredients:<slug>".
 */
class ChecklistStore(private val prefs: SharedPreferences) {

    fun get(slug: String): Set<String> =
        prefs.getStringSet(key(slug), emptySet()) ?: emptySet()

    fun set(slug: String, items: Set<String>) {
        prefs.edit().putStringSet(key(slug), items).apply()
    }

    fun clear(slug: String) {
        prefs.edit().remove(key(slug)).apply()
    }

    private fun key(slug: String) = "ingredients:$slug"

    companion object {
        fun from(context: Context): ChecklistStore =
            ChecklistStore(
                context.applicationContext.getSharedPreferences(
                    "cuisine",
                    Context.MODE_PRIVATE,
                ),
            )
    }
}
