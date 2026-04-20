package fr.vferries.cuisine.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.vferries.cuisine.data.RecipeRepository
import fr.vferries.cuisine.ui.theme.ThemeMode

@Composable
fun CuisineNavHost(
    repository: RecipeRepository,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            val vm: HomeViewModel = viewModel(
                factory = factoryOf { HomeViewModel(repository) },
            )
            val state by vm.state.collectAsState()
            HomeScreen(
                state = state,
                onRecipeClick = { slug -> nav.navigate("recipe/$slug") },
                onSettingsClick = { nav.navigate("settings") },
            )
        }
        composable("settings") {
            SettingsScreen(
                mode = themeMode,
                onModeChange = onThemeModeChange,
                onBack = { nav.popBackStack() },
            )
        }
        composable(
            route = "recipe/{slug}",
            arguments = listOf(navArgument("slug") { type = NavType.StringType }),
        ) { entry ->
            val slug = entry.arguments?.getString("slug").orEmpty()
            val vm: RecipeViewModel = viewModel(
                key = "recipe-$slug",
                factory = factoryOf { RecipeViewModel(repository, slug) },
            )
            val state by vm.state.collectAsState()
            RecipeScreen(
                state = state,
                onStartCuisson = { nav.navigate("cuisson/$slug") },
                onBack = { nav.popBackStack() },
            )
        }
        composable(
            route = "cuisson/{slug}",
            arguments = listOf(navArgument("slug") { type = NavType.StringType }),
        ) { entry ->
            val slug = entry.arguments?.getString("slug").orEmpty()
            val vm: RecipeViewModel = viewModel(
                key = "cuisson-$slug",
                factory = factoryOf { RecipeViewModel(repository, slug) },
            )
            val state by vm.state.collectAsState()
            CuissonScreen(state = state, onExit = { nav.popBackStack() })
        }
    }
}

private inline fun <reified VM : androidx.lifecycle.ViewModel> factoryOf(
    crossinline create: () -> VM,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = create() as T
}
