package fr.vferries.cuisine.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.vferries.cuisine.data.Recipe
import fr.vferries.cuisine.data.StepToken
import fr.vferries.cuisine.data.Urls
import fr.vferries.cuisine.data.formatQty
import fr.vferries.cuisine.data.formatUnit
import fr.vferries.cuisine.data.pluralizeName

@Composable
fun RecipeScreen(state: RecipeState) {
    when (state) {
        RecipeState.Loading -> Text(
            text = "Chargement…",
            modifier = Modifier.padding(16.dp),
        )
        is RecipeState.Error -> Text(
            text = "Erreur : ${state.message}",
            modifier = Modifier.padding(16.dp),
        )
        is RecipeState.Success -> SuccessContent(state.recipe)
    }
}

@Composable
private fun SuccessContent(recipe: Recipe) {
    val title = recipe.metadata["title"].orEmpty()
    val hasImage = recipe.metadata["image"]?.isNotBlank() == true
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (hasImage) {
            item {
                AsyncImage(
                    model = Urls.heroUrl(recipe.slug),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(14.dp)),
                )
            }
        }
        item { Text(text = title, style = MaterialTheme.typography.headlineMedium) }
        if (recipe.ingredients.isNotEmpty()) {
            item { Text(text = "Ingrédients", style = MaterialTheme.typography.titleMedium) }
            items(recipe.ingredients, key = { it.name }) { ing ->
                val qty = formatQty(ing.quantity, ing.unit)
                Row {
                    Text(text = ing.name, modifier = Modifier.weight(1f))
                    Text(text = qty ?: "au goût")
                }
            }
            item { HorizontalDivider() }
        }
        if (recipe.cookware.isNotEmpty()) {
            item { Text(text = "Ustensiles", style = MaterialTheme.typography.titleMedium) }
            item {
                val line = recipe.cookware.joinToString(", ") { c ->
                    val q = c.quantity.toIntOrNull() ?: 1
                    if (q > 1) "$q ${pluralizeName(q, c.name)}" else c.name
                }
                Text(text = line)
            }
            item { HorizontalDivider() }
        }
        recipe.sections.forEach { section ->
            item { Text(text = section.name, style = MaterialTheme.typography.titleMedium) }
            items(section.steps) { step ->
                Text(text = step.renderText())
            }
        }
        if (recipe.tips.isNotEmpty()) {
            item { HorizontalDivider() }
            item { Text(text = "Astuces", style = MaterialTheme.typography.titleMedium) }
            items(recipe.tips) { Text(text = it) }
        }
    }
}

private fun fr.vferries.cuisine.data.Step.renderText(): String =
    tokens.joinToString("") { token ->
        when (token) {
            is StepToken.Text -> token.text
            is StepToken.IngredientToken -> token.ingredient.name
            is StepToken.CookwareToken -> token.cookware.name
            is StepToken.TimerToken -> buildString {
                append(token.timer.quantity)
                val u = formatUnit(token.timer.quantity, token.timer.unit)
                if (u.isNotEmpty()) append(' ').append(u)
            }
        }
    }
