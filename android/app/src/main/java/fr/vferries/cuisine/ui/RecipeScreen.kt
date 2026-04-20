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
import androidx.compose.foundation.clickable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.vferries.cuisine.data.Recipe
import fr.vferries.cuisine.data.StepToken
import fr.vferries.cuisine.data.Urls
import fr.vferries.cuisine.data.checklist.ChecklistStore
import fr.vferries.cuisine.data.formatQty
import fr.vferries.cuisine.data.formatUnit
import fr.vferries.cuisine.data.pluralizeName
import fr.vferries.cuisine.data.scaleQuantityText

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
    val originalServings = recipe.metadata["servings"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
    var currentServings by rememberSaveable(recipe.slug) { mutableIntStateOf(originalServings) }
    val ratio = currentServings.toDouble() / originalServings

    val context = LocalContext.current
    val store = remember { ChecklistStore.from(context) }
    var checked by remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(recipe.slug) { checked = store.get(recipe.slug) }
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
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = "Portions", modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = { if (currentServings > 1) currentServings-- },
                    enabled = currentServings > 1,
                ) { Text("−") }
                Text(text = currentServings.toString())
                OutlinedButton(onClick = { currentServings++ }) { Text("+") }
            }
        }
        if (recipe.ingredients.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ingrédients",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    if (checked.isNotEmpty()) {
                        TextButton(onClick = {
                            checked = emptySet()
                            store.set(recipe.slug, emptySet())
                        }) { Text("Tout décocher") }
                    }
                }
            }
            items(recipe.ingredients, key = { it.name }) { ing ->
                val isChecked = ing.name in checked
                val scaled = scaleQuantityText(ing.quantity, ratio)
                val qty = formatQty(scaled, ing.unit)
                val deco = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val next = if (isChecked) checked - ing.name else checked + ing.name
                            checked = next
                            store.set(recipe.slug, next)
                        },
                ) {
                    Checkbox(checked = isChecked, onCheckedChange = null)
                    Text(
                        text = ing.name,
                        modifier = Modifier.weight(1f),
                        textDecoration = deco,
                    )
                    Text(text = qty ?: "au goût", textDecoration = deco)
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
