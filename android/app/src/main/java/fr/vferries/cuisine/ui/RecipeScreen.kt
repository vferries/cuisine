package fr.vferries.cuisine.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import fr.vferries.cuisine.data.timers.RunningTimer
import fr.vferries.cuisine.data.timers.TimerRegistry
import fr.vferries.cuisine.data.timers.timerDurationSeconds

private enum class RecipeTab(val label: String) {
    INGREDIENTS("Ingrédients"),
    STEPS("Étapes"),
    COOKWARE("Ustensiles"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    state: RecipeState,
    onStartCuisson: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val title = (state as? RecipeState.Success)?.recipe?.metadata?.get("title").orEmpty()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                actions = {
                    if (state is RecipeState.Success) {
                        Button(
                            onClick = onStartCuisson,
                            modifier = Modifier.padding(end = 8.dp),
                        ) { Text("Mode cuisson") }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
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
            is RecipeState.Success -> SuccessContent(
                recipe = state.recipe,
                contentPadding = padding,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SuccessContent(recipe: Recipe, contentPadding: PaddingValues) {
    val hasImage = recipe.metadata["image"]?.isNotBlank() == true
    val originalServings = recipe.metadata["servings"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
    var currentServings by rememberSaveable(recipe.slug) { mutableIntStateOf(originalServings) }
    val ratio = currentServings.toDouble() / originalServings

    val context = LocalContext.current
    val store = remember { ChecklistStore.from(context) }
    var checked by remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(recipe.slug) { checked = store.get(recipe.slug) }

    var tab by rememberSaveable(recipe.slug) { mutableStateOf(RecipeTab.INGREDIENTS) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        if (hasImage) {
            item(key = "hero") {
                AsyncImage(
                    model = Urls.heroUrl(recipe.slug),
                    contentDescription = recipe.metadata["title"].orEmpty(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f),
                )
            }
        }
        stickyHeader(key = "controls") {
            Surface(tonalElevation = 3.dp) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(text = "Portions", modifier = Modifier.weight(1f))
                        OutlinedButton(
                            onClick = { if (currentServings > 1) currentServings-- },
                            enabled = currentServings > 1,
                        ) { Text("−") }
                        Text(text = currentServings.toString())
                        OutlinedButton(onClick = { currentServings++ }) { Text("+") }
                    }
                    TabRow(selectedTabIndex = tab.ordinal) {
                        RecipeTab.entries.forEach { t ->
                            Tab(
                                selected = tab == t,
                                onClick = { tab = t },
                                text = { Text(t.label) },
                            )
                        }
                    }
                }
            }
        }
        when (tab) {
            RecipeTab.INGREDIENTS -> ingredientsItems(
                recipe = recipe,
                checked = checked,
                ratio = ratio,
                onToggle = { name ->
                    val next = if (name in checked) checked - name else checked + name
                    checked = next
                    store.set(recipe.slug, next)
                },
                onClearAll = {
                    checked = emptySet()
                    store.set(recipe.slug, emptySet())
                },
            )
            RecipeTab.STEPS -> stepsItems(recipe = recipe, slug = recipe.slug)
            RecipeTab.COOKWARE -> cookwareItems(recipe = recipe)
        }
    }
}

private fun LazyListScope.ingredientsItems(
    recipe: Recipe,
    checked: Set<String>,
    ratio: Double,
    onToggle: (String) -> Unit,
    onClearAll: () -> Unit,
) {
    if (checked.isNotEmpty()) {
        item(key = "clear") {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                TextButton(onClick = onClearAll) { Text("Tout décocher") }
            }
        }
    }
    items(recipe.ingredients, key = { "ing-${it.name}" }) { ing ->
        val isChecked = ing.name in checked
        val scaled = scaleQuantityText(ing.quantity, ratio)
        val qty = formatQty(scaled, ing.unit)
        val deco = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable { onToggle(ing.name) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
}

@OptIn(ExperimentalLayoutApi::class)
private fun LazyListScope.stepsItems(recipe: Recipe, slug: String) {
    recipe.sections.forEachIndexed { sectionIdx, section ->
        item(key = "section-$sectionIdx") {
            Text(
                text = section.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        section.steps.forEachIndexed { stepIdx, step ->
            item(key = "step-$sectionIdx-$stepIdx") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    step.tokens.forEachIndexed { tokIdx, token ->
                        when (token) {
                            is StepToken.Text -> Text(text = token.text)
                            is StepToken.IngredientToken -> Text(text = token.ingredient.name)
                            is StepToken.CookwareToken -> Text(text = token.cookware.name)
                            is StepToken.TimerToken -> {
                                val seconds = timerDurationSeconds(token.timer.quantity, token.timer.unit)
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
                                                    id = "$slug:$sectionIdx:$stepIdx:$tokIdx",
                                                    name = token.timer.name ?: section.name,
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
    }
    if (recipe.tips.isNotEmpty()) {
        item(key = "tips-card") {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "ASTUCES",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    recipe.tips.forEachIndexed { i, tip ->
                        if (i > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                            )
                        }
                        Text(text = tip, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

private fun LazyListScope.cookwareItems(recipe: Recipe) {
    item(key = "cookware") {
        if (recipe.cookware.isEmpty()) {
            Text(
                text = "Aucun ustensile listé.",
                modifier = Modifier.padding(16.dp),
            )
        } else {
            val line = recipe.cookware.joinToString(", ") { c ->
                val q = c.quantity.toIntOrNull() ?: 1
                if (q > 1) "$q ${pluralizeName(q, c.name)}" else c.name
            }
            Text(text = line, modifier = Modifier.padding(16.dp))
        }
    }
}
