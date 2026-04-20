package fr.vferries.cuisine.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.vferries.cuisine.data.RecipeMeta
import fr.vferries.cuisine.data.Urls

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
                RecipeRow(recipe = recipe, onClick = { onRecipeClick(recipe.slug) })
            }
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
