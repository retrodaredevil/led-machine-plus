package me.retrodaredevil.token

import me.retrodaredevil.util.getLogger

data class ParsePair(
        val startPattern: String,
        val endPattern: String,
        val condenseTokens: (List<Token>) -> Token,
)

val COMMENT_PARSE_PAIR = ParsePair("/*", "*/") { NothingToken }
val SINGLE_LINE_COMMENT_PARSE_PAIR = ParsePair("//", "\n") { NothingToken }
val PARENTHESIS_PARSE_PAIR = ParsePair("(", ")", ::OrganizerToken)

object TokenParse {
    private val LOGGER = getLogger()

    private fun selectToken(subtext: String, staticTokens: List<StaticToken>): StaticToken? {
        return staticTokens.firstOrNull { subtext.startsWith(it.pattern) }
    }
    private fun selectStartParsePair(subtext: String, parsePairs: List<ParsePair>): ParsePair? {
        return parsePairs.firstOrNull { subtext.startsWith(it.startPattern) }
    }
    private fun parseToTokens(start: Int, currentParsePair: ParsePair?, text: String, staticTokens: List<StaticToken>, parsePairs: List<ParsePair>): ParseResult {
        var stringData = ""
        val tokens = mutableListOf<Token>()
        fun reset() {
            if (stringData.isNotEmpty()) {
                tokens.add(StringToken(stringData))
                stringData = ""
            }
        }
        var position = start
        while (position < text.length) {
            val subtext = text.substring(position)
            if (currentParsePair != null && subtext.startsWith(currentParsePair.endPattern)) {
                reset()
                position += currentParsePair.endPattern.length
                return ParseResult(position, true, tokens)
            }
            val staticToken = selectToken(subtext, staticTokens)
            if (staticToken != null) {
                reset()
                position += staticToken.pattern.length
                tokens.add(staticToken)
                continue
            }
            val parsePair = selectStartParsePair(subtext, parsePairs)
            if (parsePair != null) {
                reset()
                position += parsePair.startPattern.length
                val innerResult = parseToTokens(position, parsePair, text, staticTokens, parsePairs)
                position = innerResult.endPosition
                if (!innerResult.endedParsePair) {
                    // Note that the Python variant of this had a TODO for making this an exception.
                    //   So come back to this later and maybe look at the Python code again to decide if that's what we want to do
                    LOGGER.debug("endedParsePair is false! Something didn't end with a ${parsePair.endPattern}")
                }
                tokens.add(parsePair.condenseTokens(innerResult.tokens))
                continue
            }
            stringData += text[position]
            position += 1
        }
        reset()
        return ParseResult(position, false, tokens)
    }

    fun parseToTokens(text: String, staticTokens: List<StaticToken>, parsePairs: List<ParsePair>): List<Token> {
        val result = parseToTokens(0, null, text, staticTokens, parsePairs)
        assert(result.endPosition <= text.length)
        if (result.endPosition < text.length) {
            LOGGER.debug("Did not exhaust text. endPosition=${result.endPosition}")
        }
        return result.tokens
    }

    private class ParseResult(
            val endPosition: Int,
            val endedParsePair: Boolean,
            val tokens: List<Token>
    )
}
