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
