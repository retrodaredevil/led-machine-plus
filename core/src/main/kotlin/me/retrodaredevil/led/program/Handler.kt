package me.retrodaredevil.led.program

import me.retrodaredevil.led.*
import me.retrodaredevil.led.alter.AlterSolid
import me.retrodaredevil.led.token.*
import me.retrodaredevil.token.*


class MessageContext {
    var reset: Boolean = false
    var fullReset: Boolean = false
}

private fun toTokens(text: String): List<Token> {
    return TokenParse.parseToTokens(text, listOf(PARTITION_TOKEN, BLEND_TOKEN), listOf(COMMENT_PARSE_PAIR, SINGLE_LINE_COMMENT_PARSE_PAIR, PARENTHESIS_PARSE_PAIR, VARIABLE_ASSIGN_PARSE_PAIR, VARIABLE_PARSE_PAIR, REPEAT_PARSE_PAIR))
}

fun handleMessage(rawText: String, ledState: LedState, context: MessageContext) {
    // Get the assignments without any bloat -- assignments should give raw strings, which is why we don't have as many parse pairs
    val assignmentTokens = TokenParse.parseToTokens(rawText, listOf(), listOf(COMMENT_PARSE_PAIR, SINGLE_LINE_COMMENT_PARSE_PAIR, VARIABLE_ASSIGN_PARSE_PAIR))
    assignmentTokens.mapNotNull { it as? VariableAssignmentToken }.forEach { variableAssignmentToken ->
        ledState.variableMap[variableAssignmentToken.name] = variableAssignmentToken.value
    }
    // Replace the text with our variables
    var text = rawText // we could consider doing variable replacement after parsing to tokens, then recursively parsing variables to tokens
    for (i in 1..100) { // max recursion depth of 100 should be good
        var anyChanged = false
        // TODO iterate over longer names first so that something like coolThing and coolThing2 get replaced properly
        ledState.variableMap.forEach { (name, value) ->
            val newText = text.replace("$$name", value)
            if (newText != text) {
                anyChanged = true
            }
            text = newText
        }
        if (!anyChanged) {
            break
        }
    }
    val tokens = TokenParse.parseToTokens(text, listOf(PARTITION_TOKEN, BLEND_TOKEN), listOf(COMMENT_PARSE_PAIR, SINGLE_LINE_COMMENT_PARSE_PAIR, PARENTHESIS_PARSE_PAIR, VARIABLE_ASSIGN_PARSE_PAIR, VARIABLE_PARSE_PAIR, REPEAT_PARSE_PAIR))
    println(tokens)
    // TODO give a default currentPartitionOffset for the CreatorSettings
    val creatorReference = arrayOf<AlterCreator?>(null)
    val creator = Parse.tokensToCreator(
            tokens, ledState::parseColorAlter, ledState::parsePatternAlter, Parse::getTimeMultiplier,
            CreatorSettings(0) {
                val creator = creatorReference[0]
                if (creator == null || creator.creatorData.hasColor) {
                    ledState.colorTimeMultiplier
                } else {
                    ledState.patternTimeMultiplier
                }
            }
    )
    creatorReference[0] = creator

    val isOff = "off" in text
    val colorPresent = creator != null && creator.creatorData.hasColor
    val patternPresent = creator != null && creator.creatorData.hasPattern
    if (colorPresent) {
        creator!!
        ledState.mainAlter = creator.create(ledState.startPixelSkipCount, ledState.virtualPixelCount, ledState.totalPixelCount - ledState.endPixelSkipCount, ledState.startPixelSkipCount)
    } else if (patternPresent) {
        creator!!
        ledState.patternAlter = creator.create(ledState.startPixelSkipCount, ledState.virtualPixelCount, ledState.totalPixelCount - ledState.endPixelSkipCount, ledState.startPixelSkipCount)
    } else if (isOff) {
        context.reset = true
        context.fullReset = true
        ledState.mainAlter = AlterSolid(Color.BLACK)
    }
    if (context.reset || "reset" in text) {
        ledState.reset()
    }
    var timeMultiplier: Double? = null
    if (creator != null && creator.creatorData.directlyNestedTimeMultiplier != null) {
        timeMultiplier = creator.creatorData.directlyNestedTimeMultiplier
    } else if (!colorPresent && !patternPresent) {
        timeMultiplier = Parse.getTimeMultiplier(text)
    }
    if (timeMultiplier != null) {
        if ((!colorPresent && patternPresent) || "pattern" in text) {
            // pattern speed
            ledState.patternTimeMultiplier = timeMultiplier
        } else {
            // color speed
            ledState.colorTimeMultiplier = timeMultiplier
        }
    }

    var newDim: Double? = null
    when {
        "bright" in text -> {
            newDim = 1.0
        }
        "normal" in text -> {
            newDim = 0.8
        }
        "dim" in text -> {
            newDim = 0.3 * 0.8
        }
        "dark" in text -> {
            newDim = 0.07 * 0.8
        }
        "sleep" in text -> {
            newDim = 0.008
        }
        "skyline" in text -> {
            newDim = 0.005
        }
        else -> {
            if (context.reset) {
                newDim = 0.8;
            }
        }
    }
    if (newDim != null) {
        ledState.dim = newDim
    }
}
