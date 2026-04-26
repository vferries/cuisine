package fr.vferries.cuisine.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import fr.vferries.cuisine.data.Recipe
import fr.vferries.cuisine.data.Section
import fr.vferries.cuisine.data.Step
import fr.vferries.cuisine.data.StepToken
import fr.vferries.cuisine.data.Timer
import fr.vferries.cuisine.data.timers.TimerRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CuissonScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val recipe = Recipe(
        slug = "fixture",
        metadata = mapOf("title" to "F", "cuisine" to "x", "servings" to "1"),
        sections = listOf(
            Section(
                name = "Cuisson",
                steps = listOf(
                    Step(
                        tokens = listOf(
                            StepToken.Text("Cuire pendant "),
                            StepToken.TimerToken(Timer(name = null, quantity = "3", unit = "min")),
                            StepToken.Text("."),
                        ),
                    ),
                ),
            ),
        ),
        ingredients = emptyList(),
        updatedAt = "now",
    )

    @Before
    fun resetRegistry() {
        TimerRegistry.state.value.toList().forEach { TimerRegistry.stop(it.id) }
    }

    @Test
    fun clicking_timer_chip_starts_a_timer_with_stable_id() {
        composeRule.setContent {
            CuissonScreen(state = RecipeState.Success(recipe), onExit = {})
        }
        composeRule.onNodeWithText("3 min").performClick()

        val running = TimerRegistry.state.value
        assertEquals(1, running.size)
        assertEquals("fixture:0:0:1", running.first().id)
        assertEquals(180, running.first().durationSeconds)
    }
}
