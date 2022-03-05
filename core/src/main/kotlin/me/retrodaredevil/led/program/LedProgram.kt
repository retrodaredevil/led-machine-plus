package me.retrodaredevil.led.program

import me.retrodaredevil.led.Color
import me.retrodaredevil.led.Parse
import me.retrodaredevil.led.alter.*
import me.retrodaredevil.led.percent.PercentGetter
import me.retrodaredevil.led.percent.ReversingPercentGetter
import me.retrodaredevil.led.config.BaseConfig
import me.retrodaredevil.led.message.MessageQueue
import me.retrodaredevil.led.percent.BouncePercentGetter
import me.retrodaredevil.led.percent.TimeMultiplierPercentGetter
import me.retrodaredevil.util.getLogger
import kotlin.jvm.Throws
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object LedConstants {
    val defaultPercentGetter: PercentGetter = ReversingPercentGetter(2.0, 10.0 * 60, 2.0)
    val quickBoundPercentGetter: PercentGetter = BouncePercentGetter(12.0)
    val slowDefaultPercentGetter: PercentGetter = ReversingPercentGetter(4.0, 10.0 * 60, 4.0)
}

class LedState(
        val totalPixelCount: Int,
        val virtualPixelCount: Int,
        val startPixelSkipCount: Int,
        val endPixelSkipCount: Int,
) {
    private val colorPercentGetter = LedConstants.defaultPercentGetter
    private val solidColorPercentGetter = ReversingPercentGetter(10.0, 15.0 * 60, 10.0)

    var colorTimeMultiplier = 1.0
    var patternTimeMultiplier = 1.0

    var mainAlter: Alter = AlterNothing
    var patternAlter: Alter = AlterNothing

    fun reset() {
        colorTimeMultiplier = 1.0
        patternTimeMultiplier = 1.0
        patternAlter = AlterNothing
    }

    fun parseColorAlter(text: String, timeMultiplierGetter: () -> Double): Alter? {
        val requestedColors = Parse.parseColors(text)

        if ("pixel" in text && requestedColors.size >= 2) {
            return AlterBlock(
                    requestedColors.map { AlterBlock.Block(it, 1.0) },
                    TimeMultiplierPercentGetter(colorPercentGetter, timeMultiplierGetter),
                    fadeWidth = 0.0  // no fade
            )
        } else if ("rainbow" in text || requestedColors.size >= 2) {
            var patternSize = virtualPixelCount / 8.0
            var percentGetter = colorPercentGetter
            if ("double" in text && "long" in text) {
                patternSize = virtualPixelCount * 2.0
            } else if ("long" in text) {
                patternSize = virtualPixelCount.toDouble()
            } else if ("fat" in text) {
                patternSize = virtualPixelCount / 4.0
            } else if ("tiny" in text) {
                patternSize = virtualPixelCount / 16.0
            } else if ("solid" in text) {
                patternSize = 3000000000.0
                percentGetter = solidColorPercentGetter
            }
            if (requestedColors.size >= 2) {
                return AlterFade(
                        TimeMultiplierPercentGetter(percentGetter, timeMultiplierGetter),
                        requestedColors.map { AlterSolid(it) },
                        patternSize
                )
            } else {
                return AlterRainbow(TimeMultiplierPercentGetter(percentGetter, timeMultiplierGetter), patternSize)
            }
        } else if (requestedColors.size == 1) {
            return AlterSolid(requestedColors[0])
        }
        return null
    }
    fun parsePatternAlter(text: String, timeMultiplierGetter: () -> Double): Alter? {
        if ("carnival" in text) {
            val blockList = when {
                "short" in text || "tiny" in text -> listOf(AlterBlock.Block(null, 3.0), AlterBlock.Block(Color.BLACK, 2.0))
                "long" in text -> listOf(AlterBlock.Block(null, 10.0), AlterBlock.Block(Color.BLACK, 6.0))
                else -> listOf(AlterBlock.Block(null, 5.0), AlterBlock.Block(Color.BLACK, 3.0))
            }
            return AlterBlock(blockList, TimeMultiplierPercentGetter(LedConstants.slowDefaultPercentGetter, timeMultiplierGetter))
        } else if ("star" in text) {
            return AlterStar(totalPixelCount, 300, timeMultiplierGetter)
        } else if ("twinkle" in text) {
            val number = Parse.getStringBefore(text, "twinkle")?.toDoubleOrNull()
            val twinklePercent = if (number != null && number in 0.0..100.0) number / 100.0 else 0.5
            val minPercent = max(0.0, twinklePercent.pow(2) - 0.1)
            val maxPercent = min(1.0, twinklePercent.pow(0.5) + 0.1)
            return AlterSpeedOfAlter(
                    AlterTwinkle(totalPixelCount, minPercent, maxPercent),
                    timeMultiplierGetter
            )
        }
        return null
    }
}

class LedProgram(
        baseConfig: BaseConfig
): AutoCloseable {
    private val messageQueue: MessageQueue = baseConfig.messageConfig.toMessageQueueCreator().createMessageQueue()
    private val virtualPixelCount = baseConfig.ledCount - (baseConfig.startPixelSkipCount + baseConfig.endPixelSkipCount)

    private val ledState = LedState(baseConfig.ledCount, virtualPixelCount, baseConfig.startPixelSkipCount, baseConfig.endPixelSkipCount)

    init {
        handleMessage("rainbow", ledState, MessageContext())
    }

    fun update(seconds: Double) {
        for (message in messageQueue.popNewMessages()) {
            val context = MessageContext()
            LOGGER.debug("Got message: ${message.text}")
            handleMessage(message.text, ledState, context)
            LOGGER.debug("alter is now: ${getAlter()}")
        }
    }
    fun getAlter(): Alter {
        return AlterMultiplexer(listOf(
                ledState.mainAlter,
                ledState.patternAlter,
                // TODO do something with endPixelSkipCount
//                AlterBlock(listOf(AlterBlock.Block(Color.BLACK, ledState.startPixelSkipCount.toDouble()), AlterBlock.Block(null, virtualPixelCount.toDouble())), PercentGetter { 0.0 }, fadeWidth = 0.0)
        ))
    }

    @Throws(Exception::class)
    override fun close() {
        messageQueue.close()
    }

    companion object {
        private val LOGGER = getLogger()
    }
}
