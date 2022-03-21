package me.retrodaredevil.led.token

import me.retrodaredevil.token.ParsePair
import me.retrodaredevil.token.StringToken
import me.retrodaredevil.token.Token

val REPEAT_PARSE_PAIR = ParsePair("{", "}", true) { tokens ->
    if (tokens.isEmpty()) {
        return@ParsePair tokens
    }
    val firstToken = tokens[0]
    if (firstToken is StringToken) {
        val string = firstToken.data
        val split = string.split(":", limit=2)
        if (split.size <= 1) {
            return@ParsePair tokens
        }
        val times = split[0].toIntOrNull()
        val content = split[1]
        val tokensToDuplicate = listOf(StringToken(content)) + tokens.subList(1, tokens.size)
        if (times == null || times <= 1) {
            return@ParsePair tokensToDuplicate
        }
        val r = mutableListOf<Token>()
        for (i in 0 until times) {
            r.addAll(tokensToDuplicate)
        }
        r
    } else {
        tokens
    }
}
