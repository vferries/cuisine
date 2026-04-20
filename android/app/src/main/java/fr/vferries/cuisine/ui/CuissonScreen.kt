package fr.vferries.cuisine.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import fr.vferries.cuisine.data.FlatStep
import fr.vferries.cuisine.data.Recipe
import fr.vferries.cuisine.data.StepToken
import fr.vferries.cuisine.data.flattenSteps
import fr.vferries.cuisine.data.formatUnit

@Composable
fun CuissonScreen(
    state: RecipeState,
    onExit: () -> Unit,
) {
    when (state) {
        RecipeState.Loading -> Text(text = "Chargement…", modifier = Modifier.padding(16.dp))
        is RecipeState.Error -> Text(text = "Erreur : ${state.message}", modifier = Modifier.padding(16.dp))
        is RecipeState.Success -> CuissonContent(recipe = state.recipe, onExit = onExit)
    }
}

@Composable
private fun CuissonContent(recipe: Recipe, onExit: () -> Unit) {
    val steps = remember(recipe.slug) { flattenSteps(recipe.sections) }
    if (steps.isEmpty()) {
        Text(text = "Aucune étape.", modifier = Modifier.padding(16.dp))
        return
    }

    KeepScreenOn()

    var index by rememberSaveable(recipe.slug) { mutableIntStateOf(0) }
    val clamped = index.coerceIn(0, steps.size - 1)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TextButton(onClick = onExit) { Text("← Quitter") }
            Text(
                text = "Étape ${clamped + 1} / ${steps.size}",
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            val current = steps[clamped]
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = current.sectionName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = current.renderText(),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedButton(
                onClick = { if (index > 0) index-- },
                enabled = clamped > 0,
                modifier = Modifier.weight(1f),
            ) { Text("Précédent") }
            Button(
                onClick = { if (index < steps.size - 1) index++ },
                enabled = clamped < steps.size - 1,
                modifier = Modifier.weight(1f),
            ) { Text("Suivant") }
        }
    }
}

@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

private fun FlatStep.renderText(): String =
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
