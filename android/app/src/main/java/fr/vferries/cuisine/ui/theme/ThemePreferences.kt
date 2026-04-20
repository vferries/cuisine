package fr.vferries.cuisine.ui.theme

import android.content.Context
import android.content.SharedPreferences

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class ThemePreferences(private val prefs: SharedPreferences) {

    fun get(): ThemeMode = runCatching {
        ThemeMode.valueOf(prefs.getString(KEY, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    }.getOrDefault(ThemeMode.SYSTEM)

    fun set(mode: ThemeMode) {
        prefs.edit().putString(KEY, mode.name).apply()
    }

    companion object {
        private const val KEY = "mode"

        fun from(context: Context): ThemePreferences =
            ThemePreferences(
                context.applicationContext.getSharedPreferences("theme", Context.MODE_PRIVATE),
            )
    }
}
