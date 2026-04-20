package fr.vferries.cuisine.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * kotlinx.serialization renders JSON number and string uniformly into this class
 * via [QuantitySerializer]. On ne garde que la forme texte — suffisant pour
 * l'affichage ; le scaling dynamique côté Android viendra plus tard.
 */
@Serializable
data class Ingredient(
    val name: String,
    @Serializable(with = QuantitySerializer::class)
    val quantity: String = "",
    val unit: String? = null,
)

@Serializable
data class Cookware(
    val name: String,
    @Serializable(with = QuantitySerializer::class)
    val quantity: String = "",
)

@Serializable
data class Timer(
    val name: String? = null,
    @Serializable(with = QuantitySerializer::class)
    val quantity: String = "",
    val unit: String = "",
)

@Serializable
sealed interface StepToken {
    @Serializable
    @SerialName("text")
    data class Text(val text: String) : StepToken

    @Serializable
    @SerialName("ingredient")
    data class IngredientToken(val ingredient: Ingredient) : StepToken

    @Serializable
    @SerialName("cookware")
    data class CookwareToken(val cookware: Cookware) : StepToken

    @Serializable
    @SerialName("timer")
    data class TimerToken(val timer: Timer) : StepToken
}

@Serializable
data class Step(val tokens: List<StepToken>)

@Serializable
data class Section(
    val name: String,
    val steps: List<Step>,
)

@Serializable
data class Recipe(
    val slug: String,
    val metadata: Map<String, String> = emptyMap(),
    val sections: List<Section> = emptyList(),
    val tips: List<String> = emptyList(),
    val ingredients: List<Ingredient> = emptyList(),
    val cookware: List<Cookware> = emptyList(),
    val timers: List<Timer> = emptyList(),
    val updatedAt: String = "",
)

object QuantitySerializer : KSerializer<String> {
    override val descriptor = PrimitiveSerialDescriptor("Quantity", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder ?: return ""
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonNull -> ""
            is JsonPrimitive -> element.content
            else -> ""
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}
