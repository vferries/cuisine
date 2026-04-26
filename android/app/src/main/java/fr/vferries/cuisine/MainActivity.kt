package fr.vferries.cuisine

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import fr.vferries.cuisine.data.CachedRecipeRepository
import fr.vferries.cuisine.data.HttpRecipeRepository
import fr.vferries.cuisine.data.cache.CuisineDatabase
import fr.vferries.cuisine.data.timers.TimerRegistry
import fr.vferries.cuisine.ui.CuisineNavHost
import fr.vferries.cuisine.ui.TimerTrayOverlay
import fr.vferries.cuisine.ui.theme.CuisineTheme
import fr.vferries.cuisine.ui.theme.ThemePreferences

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* user choice — no-op : notifs muettes si refusé */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val dao = CuisineDatabase.get(this).recipeDao()
        val repository = CachedRecipeRepository(HttpRecipeRepository(), dao)
        val themePrefs = ThemePreferences.from(this)
        TimerRegistry.init(applicationContext)
        ensureNotificationPermission()
        setContent {
            var mode by remember { mutableStateOf(themePrefs.get()) }
            CuisineTheme(mode = mode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        CuisineNavHost(
                            repository = repository,
                            themeMode = mode,
                            onThemeModeChange = {
                                mode = it
                                themePrefs.set(it)
                            },
                        )
                    }
                    TimerTrayOverlay()
                }
            }
        }
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
