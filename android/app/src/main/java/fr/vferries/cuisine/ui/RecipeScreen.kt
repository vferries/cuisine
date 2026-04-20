package fr.vferries.cuisine.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vferries.cuisine.data.Recipe
import fr.vferries.cuisine.data.StepToken

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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(text = title, style = MaterialTheme.typography.headlineMedium) }
        if (recipe.ingredients.isNotEmpty()) {
            item { Text(text = "Ingrédients", style = MaterialTheme.typography.titleMedium) }
            items(recipe.ingredients, key = { it.name }) { ing ->
                val qty = listOfNotNull(
                    ing.quantity.takeIf { it.isNotBlank() },
                    ing.unit?.takeIf { it.isNotBlank() },
                ).joinToString(" ")
                Row {
                    Text(text = ing.name, modifier = Modifier.weight(1f))
                    if (qty.isNotEmpty()) Text(text = qty)
                }
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
                if (token.timer.unit.isNotBlank()) append(' ').append(token.timer.unit)
            }
        }
    }
