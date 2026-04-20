package fr.vferries.cuisine.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vferries.cuisine.data.RecipeMeta

sealed interface HomeState {
    data object Loading : HomeState
    data class Success(val recipes: List<RecipeMeta>) : HomeState
    data class Error(val message: String) : HomeState
}

@Composable
fun HomeScreen(
    state: HomeState,
    onRecipeClick: (String) -> Unit = {},
) {
    when (state) {
        HomeState.Loading -> Text(text = "Chargement…", modifier = Modifier.padding(16.dp))
        is HomeState.Error -> Text(
            text = "Erreur : ${state.message}",
            modifier = Modifier.padding(16.dp),
        )
        is HomeState.Success -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.recipes, key = { it.slug }) { recipe ->
                Text(
                    text = recipe.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecipeClick(recipe.slug) }
                        .padding(vertical = 8.dp),
                )
            }
        }
    }
}
