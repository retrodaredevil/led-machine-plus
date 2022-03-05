package me.retrodaredevil.led.program

import me.retrodaredevil.led.*
import me.retrodaredevil.led.alter.AlterSolid
import me.retrodaredevil.token.COMMENT_PARSE_PAIR
import me.retrodaredevil.token.PARENTHESIS_PARSE_PAIR
import me.retrodaredevil.token.SINGLE_LINE_COMMENT_PARSE_PAIR
import me.retrodaredevil.token.TokenParse


class MessageContext {
    var reset: Boolean = false
}

fun handleMessage(text: String, ledState: LedState, context: MessageContext) {
    val tokens = TokenParse.parseToTokens(text, listOf(PARTITION_TOKEN, BLEND_TOKEN), listOf(COMMENT_PARSE_PAIR, SINGLE_LINE_COMMENT_PARSE_PAIR, PARENTHESIS_PARSE_PAIR))
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
}
