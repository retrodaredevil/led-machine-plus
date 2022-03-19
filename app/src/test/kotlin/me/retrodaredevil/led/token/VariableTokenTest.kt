package me.retrodaredevil.led.token

import me.retrodaredevil.led.BLEND_TOKEN
import me.retrodaredevil.led.PARTITION_TOKEN
import me.retrodaredevil.token.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class VariableTokenTest {
    private fun toTokens(text: String): List<Token> {
        return TokenParse.parseToTokens(text, listOf(PARTITION_TOKEN, BLEND_TOKEN), listOf(COMMENT_PARSE_PAIR, SINGLE_LINE_COMMENT_PARSE_PAIR, PARENTHESIS_PARSE_PAIR, VARIABLE_ASSIGN_PARSE_PAIR, VARIABLE_PARSE_PAIR))
    }
    @Test
    fun testAssignment() {
        val text = "[var=purple red]"
        val tokens = toTokens(text)
        assertEquals(listOf(VariableAssignmentToken("var", "purple red")), tokens)
    }
    @Test
    fun testVariable() {
        val text = "\$var "
        val tokens = toTokens(text)
        assertEquals(listOf(VariableToken("var")), tokens)
    }
    @Test
    fun test() {
        println(toTokens("< purple red >"))
    }
}
