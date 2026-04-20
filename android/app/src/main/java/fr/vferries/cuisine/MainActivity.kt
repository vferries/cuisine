package fr.vferries.cuisine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.vferries.cuisine.data.CachedRecipeRepository
import fr.vferries.cuisine.data.HttpRecipeRepository
import fr.vferries.cuisine.data.cache.CuisineDatabase
import fr.vferries.cuisine.data.timers.TimerRegistry
import fr.vferries.cuisine.ui.CuisineNavHost
import fr.vferries.cuisine.ui.TimerTrayOverlay
import fr.vferries.cuisine.ui.theme.CuisineTheme
import fr.vferries.cuisine.ui.theme.ThemePreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val dao = CuisineDatabase.get(this).recipeDao()
        val repository = CachedRecipeRepository(HttpRecipeRepository(), dao)
        val themePrefs = ThemePreferences.from(this)
        TimerRegistry.init(applicationContext)
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
}
