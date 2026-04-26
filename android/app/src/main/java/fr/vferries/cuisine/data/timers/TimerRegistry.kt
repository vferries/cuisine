package fr.vferries.cuisine.data.timers

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Registre singleton des timers actifs, persisté via SharedPreferences.
 * [init] doit être appelé avant utilisation (MainActivity.onCreate).
 */
object TimerRegistry {
    private var prefs: SharedPreferences? = null
    private var scheduler: TimerScheduler? = null
    private val fmt = Json { ignoreUnknownKeys = true }
    private val _state = MutableStateFlow<List<RunningTimer>>(emptyList())
    val state: StateFlow<List<RunningTimer>> = _state.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        val app = context.applicationContext
        prefs = app.getSharedPreferences("timers", Context.MODE_PRIVATE)
        scheduler = TimerScheduler(app)
        _state.value = read()
    }

    /** Démarre un timer. Idempotent par id. */
    fun start(timer: RunningTimer) {
        if (_state.value.any { it.id == timer.id }) return
        _state.value = _state.value + timer
        persist()
        scheduler?.schedule(timer)
    }

    fun stop(id: String) {
        _state.value = _state.value.filterNot { it.id == id }
        persist()
        scheduler?.cancel(id)
    }

    private fun read(): List<RunningTimer> {
        val raw = prefs?.getString("list", null) ?: return emptyList()
        return runCatching { fmt.decodeFromString<List<RunningTimer>>(raw) }.getOrDefault(emptyList())
    }

    private fun persist() {
        prefs?.edit()?.putString("list", fmt.encodeToString<List<RunningTimer>>(_state.value))?.apply()
    }
}
