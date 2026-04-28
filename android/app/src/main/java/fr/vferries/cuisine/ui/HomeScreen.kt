package fr.vferries.cuisine.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import fr.vferries.cuisine.data.ChipKey
import fr.vferries.cuisine.data.Course
import fr.vferries.cuisine.data.Difficulty
import fr.vferries.cuisine.data.RecipeMeta
import fr.vferries.cuisine.data.SearchScope
import fr.vferries.cuisine.data.SortMode
import fr.vferries.cuisine.data.Urls
import fr.vferries.cuisine.data.favorites.FavoritesStore
import fr.vferries.cuisine.data.filterByChip
import fr.vferries.cuisine.data.filterByCourse
import fr.vferries.cuisine.data.filterByDifficulty
import fr.vferries.cuisine.data.filterBySansGluten
import fr.vferries.cuisine.data.matchingRecipeSlugs
import fr.vferries.cuisine.data.sortRecipes

sealed interface HomeState {
    data object Loading : HomeState
    data class Success(val recipes: List<RecipeMeta>) : HomeState
    data class Error(val message: String) : HomeState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onRecipeClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cuisine") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Réglages",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (state) {
            HomeState.Loading -> Text(
                text = "Chargement…",
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
            )
            is HomeState.Error -> Text(
                text = "Erreur : ${state.message}",
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
            )
            is HomeState.Success -> SuccessList(
                recipes = state.recipes,
                contentPadding = padding,
                onRecipeClick = onRecipeClick,
            )
        }
    }
}

@Composable
private fun SuccessList(
    recipes: List<RecipeMeta>,
    contentPadding: PaddingValues,
    onRecipeClick: (String) -> Unit,
) {
    val context = LocalContext.current
    val store = remember { FavoritesStore.from(context) }
    var favorites by remember { mutableStateOf(store.get()) }
    var query by rememberSaveable { mutableStateOf("") }
    var chip by rememberSaveable { mutableStateOf(ChipKey.ALL) }
    var advancedOpen by rememberSaveable { mutableStateOf(false) }
    var scope by rememberSaveable { mutableStateOf(SearchScope.ALL) }
    var course by rememberSaveable { mutableStateOf<Course?>(null) }
    var difficulty by rememberSaveable { mutableStateOf<Difficulty?>(null) }
    var sansGluten by rememberSaveable { mutableStateOf(false) }
    var sortMode by rememberSaveable { mutableStateOf(SortMode.RECENT) }
    val filtered = remember(
        recipes, query, chip, favorites, scope, course, difficulty, sansGluten, sortMode,
    ) {
        val bySearch = matchingRecipeSlugs(recipes, query, scope).toSet()
        val byChip = filterByChip(recipes, chip, favorites).toSet()
        val byCourse = filterByCourse(recipes, course).toSet()
        val byDifficulty = filterByDifficulty(recipes, difficulty).toSet()
        val byDiet = filterBySansGluten(recipes, sansGluten).toSet()
        val orderedSlugs = sortRecipes(recipes, sortMode)
        val bySlug = recipes.associateBy { it.slug }
        orderedSlugs
            .filter {
                it in bySearch && it in byChip && it in byCourse
                    && it in byDifficulty && it in byDiet
            }
            .mapNotNull { bySlug[it] }
    }
    val toggleFavorite: (String) -> Unit = { slug ->
        store.toggle(slug)
        favorites = store.get()
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Nom, ingrédient, tag…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                ChipKey.entries.forEach { key ->
                    FilterChip(
                        selected = chip == key,
                        onClick = { chip = key },
                        label = { Text(key.label) },
                    )
                }
            }
        }
        item {
            AdvancedToggle(
                open = advancedOpen,
                onClick = { advancedOpen = !advancedOpen },
            )
        }
        if (advancedOpen) {
            item {
                AdvancedPanel(
                    scope = scope,
                    onScopeChange = { scope = it },
                    course = course,
                    onCourseChange = { course = if (course == it) null else it },
                    difficulty = difficulty,
                    onDifficultyChange = { difficulty = if (difficulty == it) null else it },
                    sansGluten = sansGluten,
                    onSansGlutenChange = { sansGluten = it },
                    sortMode = sortMode,
                    onSortChange = { sortMode = it },
                )
            }
        }
        if (filtered.isEmpty()) {
            item {
                Text(
                    text = "Aucune recette",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(filtered, key = { it.slug }) { recipe ->
            RecipeRow(
                recipe = recipe,
                isFavorite = recipe.slug in favorites,
                onClick = { onRecipeClick(recipe.slug) },
                onToggleFavorite = { toggleFavorite(recipe.slug) },
            )
        }
    }
}

@Composable
private fun RecipeRow(
    recipe: RecipeMeta,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            if (recipe.image != null) {
                AsyncImage(
                    model = Urls.thumbUrl(recipe.slug),
                    contentDescription = recipe.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    text = recipe.title.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recipe.cuisine.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = listOfNotNull(
                recipe.totalTime.takeIf { it > 0 }?.let { "$it min" },
                "${recipe.servings} pers".takeIf { recipe.servings > 0 },
                recipe.difficulty.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
            if (meta.isNotEmpty()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (isFavorite) {
                    "Retirer ${recipe.title} des favoris"
                } else {
                    "Marquer ${recipe.title} comme favori"
                },
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun AdvancedToggle(open: Boolean, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (open) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "chevron",
    )
    TextButton(onClick = onClick) {
        Text(
            text = "Filtres avancés",
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Filled.ExpandMore,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .rotate(rotation),
        )
    }
}

@Composable
private fun AdvancedPanel(
    scope: SearchScope,
    onScopeChange: (SearchScope) -> Unit,
    course: Course?,
    onCourseChange: (Course) -> Unit,
    difficulty: Difficulty?,
    onDifficultyChange: (Difficulty) -> Unit,
    sansGluten: Boolean,
    onSansGlutenChange: (Boolean) -> Unit,
    sortMode: SortMode,
    onSortChange: (SortMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AdvancedGroup(label = "Recherche") {
            FilterChip(
                selected = scope == SearchScope.ALL,
                onClick = { onScopeChange(SearchScope.ALL) },
                label = { Text("Tout") },
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = scope == SearchScope.INGREDIENTS,
                onClick = { onScopeChange(SearchScope.INGREDIENTS) },
                label = { Text("Ingrédients") },
            )
        }
        AdvancedGroup(label = "Course") {
            Course.entries.forEachIndexed { idx, c ->
                if (idx > 0) Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = course == c,
                    onClick = { onCourseChange(c) },
                    label = { Text(c.label) },
                )
            }
        }
        AdvancedGroup(label = "Difficulté") {
            Difficulty.entries.forEachIndexed { idx, d ->
                if (idx > 0) Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = difficulty == d,
                    onClick = { onDifficultyChange(d) },
                    label = { Text(d.label) },
                )
            }
        }
        AdvancedGroup(label = "Régime") {
            FilterChip(
                selected = sansGluten,
                onClick = { onSansGlutenChange(!sansGluten) },
                label = { Text("Sans gluten") },
            )
        }
        AdvancedGroup(label = "Tri") {
            SortMode.entries.forEachIndexed { idx, m ->
                if (idx > 0) Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = sortMode == m,
                    onClick = { onSortChange(m) },
                    label = { Text(m.label) },
                )
            }
        }
    }
}

@Composable
private fun AdvancedGroup(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            content()
        }
    }
}
