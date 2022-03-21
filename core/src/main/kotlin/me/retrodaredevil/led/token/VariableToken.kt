package me.retrodaredevil.led.token

import me.retrodaredevil.token.NothingToken
import me.retrodaredevil.token.ParsePair
import me.retrodaredevil.token.StringToken
import me.retrodaredevil.token.Token


/** A parse pair to get stuff like <var=purple red>.*/
val VARIABLE_ASSIGN_PARSE_PAIR = ParsePair("[", "]", false) { tokens ->
    tokens.firstNotNullOfOrNull { it as? StringToken }?.let {
        val string = it.data
        val split = string.split("=")
        if (split.size == 2) {
            listOf(VariableAssignmentToken(split[0], split[1]))
        } else {
            null
        }
    } ?: listOf(NothingToken)
}
// TODO using whitespace as the ending of this ParsePair isn't ideal. We should also end it when another ParsePair's start pattern is found
val VARIABLE_PARSE_PAIR = ParsePair(Regex("^" + Regex.escape("$")), Regex("^\\s"), false) { tokens ->
    tokens.firstNotNullOfOrNull { it as? StringToken }?.let { listOf(VariableToken(it.data)) } ?: listOf(NothingToken)
}

data class VariableAssignmentToken(
        val name: String,
        val value: String,
) : Token

data class VariableToken(
        val name: String
) : Token
