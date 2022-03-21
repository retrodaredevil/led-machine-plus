package me.retrodaredevil.led.token

import me.retrodaredevil.token.NothingToken
import me.retrodaredevil.token.ParsePair
import me.retrodaredevil.token.StringToken
import me.retrodaredevil.token.Token

val REPEAT_PARSE_PAIR = ParsePair("{", "}", false) { tokens ->
    tokens.firstNotNullOfOrNull { it as? StringToken }?.let {
        val string = it.data
        val split = string.split(":")
        if (split.size == 2) {
            val times = split[0].toIntOrNull()
            val content = split[1]
            if (times != null) {
                val divider = " " // constant for now. Maybe we'll have a way in the future to change this
                var result = ""
                for (i in 0 until times) {
                    if (result.isNotEmpty()) {
                        result += divider
                    }
                    result += content
                }
                StringToken(result)
            } else {
                null
            }
        } else {
            null
        }
    } ?: NothingToken
}
