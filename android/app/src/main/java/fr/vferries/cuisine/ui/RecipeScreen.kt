package fr.vferries.cuisine.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import fr.vferries.cuisine.data.timers.RunningTimer
import fr.vferries.cuisine.data.timers.TimerRegistry
import fr.vferries.cuisine.data.timers.timerDurationSeconds

private enum class RecipeTab(val label: String) {
    INGREDIENTS("Ingrédients"),
    STEPS("Étapes"),
    COOKWARE("Ustensiles"),
}

@Composable
fun RecipeScreen(
    state: RecipeState,
    onStartCuisson: () -> Unit = {},
) {
    when (state) {
        RecipeState.Loading -> Text(
            text = "Chargement…",
            modifier = Modifier.padding(16.dp),
        )
        is RecipeState.Error -> Text(
            text = "Erreur : ${state.message}",
            modifier = Modifier.padding(16.dp),
        )
        is RecipeState.Success -> SuccessContent(state.recipe, onStartCuisson)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SuccessContent(recipe: Recipe, onStartCuisson: () -> Unit) {
    val title = recipe.metadata["title"].orEmpty()
    val hasImage = recipe.metadata["image"]?.isNotBlank() == true
    val originalServings = recipe.metadata["servings"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
    var currentServings by rememberSaveable(recipe.slug) { mutableIntStateOf(originalServings) }
    val ratio = currentServings.toDouble() / originalServings

    val context = LocalContext.current
    val store = remember { ChecklistStore.from(context) }
    var checked by remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(recipe.slug) { checked = store.get(recipe.slug) }

    var tab by rememberSaveable(recipe.slug) { mutableStateOf(RecipeTab.INGREDIENTS) }

    val listState = rememberLazyListState()
    // Le hero (item 0) est considéré comme "scrollé hors vue" dès que la liste
    // ne commence plus à l'item 0.
    val heroScrolled by remember {
        derivedStateOf { hasImage && listState.firstVisibleItemIndex > 0 }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Bottom,
                ),
            ),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        if (hasImage) {
            item(key = "hero") {
                AsyncImage(
                    model = Urls.heroUrl(recipe.slug),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f),
                )
            }
        }
        item(key = "header") {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Button(onClick = onStartCuisson) { Text("Mode cuisson") }
                }
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
        }
        stickyHeader(key = "tabs") {
            Surface(tonalElevation = 2.dp) {
                Column {
                    AnimatedVisibility(visible = heroScrolled) {
                        CompactHero(recipe = recipe, hasImage = hasImage, title = title)
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

@Composable
private fun CompactHero(recipe: Recipe, hasImage: Boolean, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        if (hasImage) {
            AsyncImage(
                model = Urls.thumbUrl(recipe.slug),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
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
        item(key = "tips-divider") {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        item(key = "tips-title") {
            Text(
                text = "Astuces",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        items(recipe.tips, key = { "tip-$it".hashCode() }) {
            Text(text = it, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
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
