package fr.vferries.cuisine.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import fr.vferries.cuisine.data.FlatStep
import fr.vferries.cuisine.data.StepToken
import fr.vferries.cuisine.data.Timer
import fr.vferries.cuisine.data.timers.TimerRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CuissonScreenTest {

    private val step = FlatStep(
        sectionName = "Cuisson",
        sectionIdx = 0,
        stepIdx = 0,
        tokens = listOf(
            StepToken.Text("Cuire pendant "),
            StepToken.TimerToken(Timer(name = null, quantity = "3", unit = "min")),
            StepToken.Text("."),
        ),
    )

    @Before
    fun resetRegistry() {
        TimerRegistry.state.value.toList().forEach { TimerRegistry.stop(it.id) }
    }

    @Test fun annotated_text_contains_full_step_phrase() {
        val annotated = stepAnnotatedString(step, slug = "fixture", linkColor = Color.Red)
        assertTrue(
            "Le texte doit être continu (était: '${annotated.text}')",
            annotated.text.contains("Cuire pendant 3 min."),
        )
    }

    @Test fun timer_token_becomes_a_clickable_link() {
        val annotated = stepAnnotatedString(step, slug = "fixture", linkColor = Color.Red)

        val links = annotated.getLinkAnnotations(0, annotated.length)
        assertEquals(1, links.size)
        val link = links.first().item
        assertTrue(link is LinkAnnotation.Clickable)
        val listener = (link as LinkAnnotation.Clickable).linkInteractionListener
        assertNotNull("Le LinkAnnotation doit porter un linkInteractionListener", listener)

        listener!!.onClick(link)

        val running = TimerRegistry.state.value
        assertEquals(1, running.size)
        assertEquals("fixture:0:0:1", running.first().id)
        assertEquals(180, running.first().durationSeconds)
    }
}
