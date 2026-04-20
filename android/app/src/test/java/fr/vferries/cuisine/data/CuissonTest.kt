package fr.vferries.cuisine.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CuissonTest {

    private fun textStep(text: String) = Step(listOf(StepToken.Text(text)))

    @Test fun flattenSteps_returns_empty_for_empty_sections() {
        assertEquals(emptyList<FlatStep>(), flattenSteps(emptyList()))
    }

    @Test fun flattenSteps_preserves_order_across_sections() {
        val flat = flattenSteps(
            listOf(
                Section("Prep", listOf(textStep("a"), textStep("b"))),
                Section("Cuisson", listOf(textStep("c"))),
            ),
        )
        assertEquals(listOf("Prep", "Prep", "Cuisson"), flat.map { it.sectionName })
        assertEquals(listOf(0 to 0, 0 to 1, 1 to 0), flat.map { it.sectionIdx to it.stepIdx })
    }
}
