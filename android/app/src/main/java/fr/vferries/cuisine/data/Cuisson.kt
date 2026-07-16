package fr.vferries.cuisine.data

data class FlatStep(
    val sectionName: String,
    val sectionIdx: Int,
    val stepIdx: Int,
    val tokens: List<StepToken>,
)

fun flattenSteps(sections: List<Section>): List<FlatStep> = buildList {
    sections.forEachIndexed { sectionIdx, section ->
        section.steps.forEachIndexed { stepIdx, step ->
            add(FlatStep(section.name, sectionIdx, stepIdx, step.tokens))
        }
    }
}

/** Index flat d'une étape (sectionIdx, stepIdx), −1 si absente. */
fun flatIndexOf(steps: List<FlatStep>, sectionIdx: Int, stepIdx: Int): Int =
    steps.indexOfFirst { it.sectionIdx == sectionIdx && it.stepIdx == stepIdx }
