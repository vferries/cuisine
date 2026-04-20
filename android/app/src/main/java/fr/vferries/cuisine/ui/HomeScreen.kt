package fr.vferries.cuisine.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.vferries.cuisine.data.ChipKey
import fr.vferries.cuisine.data.RecipeMeta
import fr.vferries.cuisine.data.Urls
import fr.vferries.cuisine.data.filterByChip
import fr.vferries.cuisine.data.matchingRecipeSlugs

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
                    TextButton(onClick = onSettingsClick) { Text("⚙") }
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
    var query by rememberSaveable { mutableStateOf("") }
    var chip by rememberSaveable { mutableStateOf(ChipKey.ALL) }
    val filtered = remember(recipes, query, chip) {
        val bySearch = matchingRecipeSlugs(recipes, query).toSet()
        val byChip = filterByChip(recipes, chip).toSet()
        recipes.filter { it.slug in bySearch && it.slug in byChip }
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
        if (filtered.isEmpty()) {
            item {
                Text(
                    text = "Aucune recette",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(filtered, key = { it.slug }) { recipe ->
            RecipeRow(recipe = recipe, onClick = { onRecipeClick(recipe.slug) })
        }
    }
}

@Composable
private fun RecipeRow(recipe: RecipeMeta, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        if (recipe.image != null) {
            AsyncImage(
                model = Urls.thumbUrl(recipe.slug),
                contentDescription = recipe.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
            Spacer(Modifier.width(12.dp))
        }
        Text(text = recipe.title, style = MaterialTheme.typography.titleMedium)
    }
}
