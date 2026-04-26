package fr.vferries.cuisine.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import fr.vferries.cuisine.data.timers.RunningTimer
import fr.vferries.cuisine.data.timers.TimerRegistry
import fr.vferries.cuisine.data.timers.timerDurationSeconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuissonScreen(
    state: RecipeState,
    onExit: () -> Unit,
) {
    val steps = (state as? RecipeState.Success)
        ?.let { flattenSteps(it.recipe.sections) }
        ?: emptyList()
    var index by rememberSaveable(
        (state as? RecipeState.Success)?.recipe?.slug ?: "loading",
    ) { mutableIntStateOf(0) }
    val clamped = if (steps.isEmpty()) 0 else index.coerceIn(0, steps.size - 1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (steps.isNotEmpty()) {
                        Text("Étape ${clamped + 1} / ${steps.size}")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quitter",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (state) {
            RecipeState.Loading -> Text(
                text = "Chargement…",
                modifier = Modifier.padding(padding).padding(16.dp),
            )
            is RecipeState.Error -> Text(
                text = "Erreur : ${state.message}",
                modifier = Modifier.padding(padding).padding(16.dp),
            )
            is RecipeState.Success -> {
                if (steps.isEmpty()) {
                    Text(
                        text = "Aucune étape.",
                        modifier = Modifier.padding(padding).padding(16.dp),
                    )
                } else {
                    KeepScreenOn()
                    CuissonStepBody(
                        step = steps[clamped],
                        slug = state.recipe.slug,
                        contentPadding = padding,
                        canPrev = clamped > 0,
                        canNext = clamped < steps.size - 1,
                        onPrev = { if (index > 0) index-- },
                        onNext = { if (index < steps.size - 1) index++ },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CuissonStepBody(
    step: FlatStep,
    slug: String,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    canPrev: Boolean,
    canNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = step.sectionName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    step.tokens.forEachIndexed { tokIdx, token ->
                        when (token) {
                            is StepToken.Text -> Text(
                                text = token.text,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            is StepToken.IngredientToken -> Text(
                                text = token.ingredient.name,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            is StepToken.CookwareToken -> Text(
                                text = token.cookware.name,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            is StepToken.TimerToken -> {
                                val seconds = timerDurationSeconds(
                                    token.timer.quantity,
                                    token.timer.unit,
                                )
                                val label = buildString {
                                    append(token.timer.quantity)
                                    val u = formatUnit(token.timer.quantity, token.timer.unit)
                                    if (u.isNotEmpty()) append(' ').append(u)
                                }
                                AssistChip(
                                    onClick = {
                                        if (seconds > 0) {
                                            TimerRegistry.start(
                                                RunningTimer(
                                                    id = "$slug:${step.sectionIdx}:${step.stepIdx}:$tokIdx",
                                                    name = token.timer.name ?: step.sectionName,
                                                    durationSeconds = seconds,
                                                    startedAtMillis = System.currentTimeMillis(),
                                                ),
                                            )
                                        }
                                    },
                                    label = { Text(label) },
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedButton(
                onClick = onPrev,
                enabled = canPrev,
                modifier = Modifier.weight(1f),
            ) { Text("Précédent") }
            Button(
                onClick = onNext,
                enabled = canNext,
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

