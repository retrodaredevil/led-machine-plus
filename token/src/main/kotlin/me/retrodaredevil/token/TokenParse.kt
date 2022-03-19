package me.retrodaredevil.token

import me.retrodaredevil.util.getLogger

data class ParsePair(
        /** The start pattern that should match when a string starts with a given pattern. This means that your regex should always start with ^*/
        val startPattern: Regex,
        val endPattern: Regex,
        val recursiveInside: Boolean,
        val condenseTokens: (List<Token>) -> Token,
) {
    constructor(startPattern: String, endPattern: String, recursiveInside: Boolean, condenseTokens: (List<Token>) -> Token)
            : this(Regex("^" + Regex.escape(startPattern)), Regex("^" + Regex.escape(endPattern)), recursiveInside, condenseTokens)
}

val COMMENT_PARSE_PAIR = ParsePair("/*", "*/", false) { NothingToken}
val SINGLE_LINE_COMMENT_PARSE_PAIR = ParsePair("//", "\n", false) { NothingToken }
val PARENTHESIS_PARSE_PAIR = ParsePair("(", ")", true, ::OrganizerToken)

object TokenParse {
    private val LOGGER = getLogger()

    private fun selectToken(subtext: String, staticTokens: List<StaticToken>): StaticToken? {
        return staticTokens.firstOrNull { subtext.startsWith(it.pattern) }
    }
    private fun selectStartParsePair(subtext: String, parsePairs: List<ParsePair>): Pair<ParsePair, Int>? {
        if (subtext.isEmpty()) {
            return null
        }
        return parsePairs.asSequence().mapNotNull {
            val matchResult = it.startPattern.find(subtext) ?: return@mapNotNull null
            val matchGroup = matchResult.groups[0]!!
            val matchLength = matchGroup.value.length
            Pair(it, matchLength)
        }.firstOrNull()
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
            if (currentParsePair != null) {
                val matchGroup = currentParsePair.endPattern.find(subtext)?.groups?.get(0)
                if (matchGroup != null) {
                    reset()
                    position += matchGroup.value.length
                    return ParseResult(position, true, tokens)
                }
            }
            if (currentParsePair == null || currentParsePair.recursiveInside) {
                val staticToken = selectToken(subtext, staticTokens)
                if (staticToken != null) {
                    reset()
                    position += staticToken.pattern.length
                    tokens.add(staticToken)
                    continue
                }
                val parsePairPair = selectStartParsePair(subtext, parsePairs)
                if (parsePairPair != null) {
                    val parsePair = parsePairPair.first
                    val startPatternLength = parsePairPair.second
                    reset()
                    position += startPatternLength
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
