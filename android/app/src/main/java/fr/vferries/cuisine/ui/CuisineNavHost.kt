package fr.vferries.cuisine.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import fr.vferries.cuisine.data.timers.TimerDeepLink
import fr.vferries.cuisine.ui.theme.ThemeMode

@Composable
fun CuisineNavHost(
    repository: RecipeRepository,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    deepLink: TimerDeepLink? = null,
    onDeepLinkConsumed: () -> Unit = {},
) {
    val nav = rememberNavController()
    LaunchedEffect(deepLink) {
        if (deepLink == null) return@LaunchedEffect
        // Pile Home → Recette → Cuisson, quel que soit l'écran courant.
        nav.navigate("recipe/${deepLink.slug}") {
            popUpTo("home")
            launchSingleTop = true
        }
        nav.navigate(
            "cuisson/${deepLink.slug}?section=${deepLink.sectionIdx}&step=${deepLink.stepIdx}",
        )
        onDeepLinkConsumed()
    }
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
            route = "cuisson/{slug}?section={section}&step={step}",
            arguments = listOf(
                navArgument("slug") { type = NavType.StringType },
                navArgument("section") { type = NavType.IntType; defaultValue = -1 },
                navArgument("step") { type = NavType.IntType; defaultValue = -1 },
            ),
        ) { entry ->
            val slug = entry.arguments?.getString("slug").orEmpty()
            val vm: RecipeViewModel = viewModel(
                key = "cuisson-$slug",
                factory = factoryOf { RecipeViewModel(repository, slug) },
            )
            val state by vm.state.collectAsState()
            CuissonScreen(
                state = state,
                onExit = { nav.popBackStack() },
                targetSection = entry.arguments?.getInt("section") ?: -1,
                targetStep = entry.arguments?.getInt("step") ?: -1,
            )
        }
    }
}

private inline fun <reified VM : androidx.lifecycle.ViewModel> factoryOf(
    crossinline create: () -> VM,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = create() as T
}
