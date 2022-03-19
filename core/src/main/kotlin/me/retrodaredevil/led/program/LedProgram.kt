package me.retrodaredevil.led.program

import me.retrodaredevil.led.Color
import me.retrodaredevil.led.Parse
import me.retrodaredevil.led.alter.*
import me.retrodaredevil.led.percent.PercentGetter
import me.retrodaredevil.led.percent.ReversingPercentGetter
import me.retrodaredevil.led.config.BaseConfig
import me.retrodaredevil.led.createDefaultMapper
import me.retrodaredevil.led.message.MessageQueue
import me.retrodaredevil.led.percent.BouncePercentGetter
import me.retrodaredevil.led.percent.TimeMultiplierPercentGetter
import me.retrodaredevil.util.getLogger
import java.io.File
import java.io.IOException
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

    val variableMap = mutableMapOf<String, String>()

    var dim = 0.8

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
        } else if ("single" in text) {
            return AlterBlock(
                    listOf(AlterBlock.Block(null, 5.0), AlterBlock.Block(Color.BLACK, virtualPixelCount - 5.0)),
                    TimeMultiplierPercentGetter(
                            { seconds -> LedConstants.quickBoundPercentGetter.getPercent(seconds) * (virtualPixelCount - 5.0) / virtualPixelCount},
                            timeMultiplierGetter
                    )
            )
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

    private val ledState = LedState(baseConfig.ledCount, baseConfig.ledCount - (baseConfig.startPixelSkipCount + baseConfig.endPixelSkipCount), baseConfig.startPixelSkipCount, baseConfig.endPixelSkipCount)

    private val messageHistory = mutableListOf<String>()

    private var alterCache: Alter? = null

    init {
        handleMessage("rainbow", ledState, MessageContext())
        val objectMapper = createDefaultMapper()
        var saveFile: SaveFile? = null
        try {
            saveFile = objectMapper.readValue(SAVE_FILE, SaveFile::class.java)
        } catch (ex: IOException) {
            LOGGER.debug("Could not read save file", ex)
        }
        if (saveFile != null) {
            for (message in saveFile.messages) {
                handleMessage(message.lowercase(), ledState, MessageContext())
            }
        }
    }

    fun update(seconds: Double) {
        for (message in messageQueue.popNewMessages()) {
            val context = MessageContext()
            LOGGER.debug("Got message: ${message.text}")
            handleMessage(message.text.lowercase(), ledState, context)
            alterCache = null
            LOGGER.debug("alter is now: ${getAlter()}")
            if (context.fullReset) {
                messageHistory.clear()
            }
            messageHistory.add(message.text)
            saveMessages()
        }
    }
    fun getAlter(): Alter {
        val cache = alterCache
        if (cache != null) {
            return cache
        }
        val alter = AlterMultiplexer(listOf(
                ledState.mainAlter,
                ledState.patternAlter,
                AlterBlock(listOf(
                        AlterBlock.Block(Color.BLACK, ledState.startPixelSkipCount.toDouble()),
                        AlterBlock.Block(null, ledState.virtualPixelCount.toDouble()),
                        AlterBlock.Block(Color.BLACK, ledState.endPixelSkipCount.toDouble()),
                ), { 0.0 }, fadeWidth = 0.0),
                AlterDim(ledState.dim),
        ))
        alterCache = alter
        return alter
    }
    private fun saveMessages() {
        val saveFile = SaveFile(messageHistory)
        val objectMapper = createDefaultMapper()
        try {
            objectMapper.writeValue(SAVE_FILE, saveFile)
        } catch (ex: IOException) {
            LOGGER.warn("Could not save file", ex)
        }
    }

    @Throws(Exception::class)
    override fun close() {
        messageQueue.close()
    }

    companion object {
        private val LOGGER = getLogger()
        private val SAVE_FILE = File("saved.json")
    }

    private class SaveFile(
            val messages: List<String>
    )
}
