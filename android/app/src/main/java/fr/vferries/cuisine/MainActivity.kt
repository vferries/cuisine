package fr.vferries.cuisine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import fr.vferries.cuisine.data.CachedRecipeRepository
import fr.vferries.cuisine.data.HttpRecipeRepository
import fr.vferries.cuisine.data.cache.CuisineDatabase
import fr.vferries.cuisine.data.timers.TimerRegistry
import fr.vferries.cuisine.ui.CuisineNavHost
import fr.vferries.cuisine.ui.TimerTrayOverlay
import fr.vferries.cuisine.ui.theme.CuisineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = CuisineDatabase.get(this).recipeDao()
        val repository = CachedRecipeRepository(HttpRecipeRepository(), dao)
        TimerRegistry.init(applicationContext)
        setContent {
            CuisineTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        CuisineNavHost(repository = repository)
                    }
                    TimerTrayOverlay()
                }
            }
        }
    }
}
