package week1

import java.text.NumberFormat
import java.util.Locale

sealed class JsonElement
data class JsonObject(val fields: Map<String, JsonElement>) : JsonElement() {
    constructor(vararg fields: Pair<String, JsonElement>) : this(fields.toMap())
}

data class JsonArray(val elements: List<JsonElement>) : JsonElement() {
    constructor(vararg elements: JsonElement) : this(elements.toList())
}

data class JsonNumber(val value: Double) : JsonElement()
data class JsonString(val value: String) : JsonElement()
data class JsonBoolean(val value: Boolean) : JsonElement()
object JsonNull : JsonElement()

fun JsonElement.stringify(): String = buildString { appendJson(this@stringify) }

private val nf = NumberFormat.getInstance(Locale.ROOT).apply {
    minimumFractionDigits = 0
}

private fun StringBuilder.appendJson(element: JsonElement) {
    when (element) {
        is JsonObject -> {
            append('{')
            var needsComma = false
            for ((key, value) in element.fields) {
                if (needsComma) append(',')
                appendEscaped(key)
                append(':')
                appendJson(value)
                needsComma = true
            }
            append('}')
        }
        is JsonArray -> {
            append('[')
            var needsComma = false
            for (child in element.elements) {
                if (needsComma) append(',')
                appendJson(child)
                needsComma = true
            }
            append(']')
        }
        is JsonNumber -> append(nf.format(element.value))
        is JsonString -> appendEscaped(element.value)
        is JsonBoolean -> append(element.value.toString())
        JsonNull -> append("null")
    }
}

private fun StringBuilder.appendEscaped(string: String) {
    append('"')
    for (char in string) {
        if (char.isISOControl()) {
            append("\\u%04X".format(char.code))
        } else {
            if (char == '"' || char == '\\') append('\\')
            append(char)
        }
    }
    append('"')
}
