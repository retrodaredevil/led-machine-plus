package me.retrodaredevil.led

import me.retrodaredevil.led.alter.*
import me.retrodaredevil.led.percent.ReversingPercentGetter
import me.retrodaredevil.led.percent.TimeMultiplierPercentGetter
import me.retrodaredevil.token.*
import me.retrodaredevil.util.getLogger

val PARTITION_TOKEN = StaticToken("partition", "|")
val BLEND_TOKEN = StaticToken("blend", "~")

sealed interface Length

class PixelLength(
        val pixelLength: Int
) : Length {
    init {
        check(pixelLength >= 0)
    }
}
class PercentLength(
        val percent: Double
) : Length {
    init {
        check(percent in 0.0..1.0)
    }
}

data class CreatorData(
        val hasColor: Boolean,
        val hasPattern: Boolean,
        val preferredLength: Length? = null,
        val directlyNestedTimeMultiplier: Double? = null
)

abstract class AlterCreator(
        val creatorData: CreatorData,
) {
    abstract fun create(startPixel: Int, pixelCount: Int, wrapAt: Int, wrapTo: Int): Alter
}

class StaticCreator(
        creatorData: CreatorData,
        private val alter: Alter,
) : AlterCreator(creatorData) {
    override fun create(startPixel: Int, pixelCount: Int, wrapAt: Int, wrapTo: Int): Alter {
        return alter
    }
}
val NOTHING_CREATOR = StaticCreator(CreatorData(hasColor = false, hasPattern = false), AlterNothing)

class CombinerCreator(
        private val colorCreators: List<AlterCreator>,
        private val patternCreators: List<AlterCreator>,
        directlyNestedTimeMultiplier: Double?,
) : AlterCreator(CreatorData(colorCreators.isNotEmpty(), patternCreators.isNotEmpty(), directlyNestedTimeMultiplier = directlyNestedTimeMultiplier)) {
    override fun create(startPixel: Int, pixelCount: Int, wrapAt: Int, wrapTo: Int): Alter {
        val colorAlters = colorCreators.map { it.create(startPixel, pixelCount, wrapAt, wrapTo) }
        val patternAlters = patternCreators.map { it.create(startPixel, pixelCount, wrapAt, wrapTo) }

        return AlterMultiplexer(colorAlters + patternAlters)
    }
}

class PartitionAlterCreator(
        private val creators: List<AlterCreator>,
        private val partitionOffset: Int
) : AlterCreator(CreatorData(
        creators.any { it.creatorData.hasColor },
        creators.any { it.creatorData.hasPattern }
)) {
    override fun create(startPixel: Int, pixelCount: Int, wrapAt: Int, wrapTo: Int): Alter {
        // TODO use preferredLength
        val pixelsPerPartition = pixelCount / creators.size
        val extraPixels = pixelCount % creators.size
        var startPixel = startPixel + partitionOffset
        if (startPixel > wrapAt) {
            startPixel += -wrapAt + wrapTo
        }
        val alterPartitions = mutableListOf<AlterPartition.Partition>()

        for ((i, creator) in creators.withIndex()) {
            val length = pixelsPerPartition + if (i < extraPixels) 1 else 0
            val endPixel = startPixel + length - 1 // The index of the last pixel in this partition
            val boundsList = mutableListOf<Pair<Double, Double>>()
            val alter: Alter
            if (endPixel >= wrapAt) {
                val endLength = wrapAt - startPixel
                val leftoverLength = length - endLength
                boundsList.add(Pair(startPixel.toDouble(), endLength.toDouble()))
                boundsList.add(Pair(wrapTo.toDouble(), leftoverLength.toDouble()))
                alter = creator.create(startPixel, length, wrapAt, wrapTo)
                startPixel = wrapTo + leftoverLength
            } else {
                boundsList.add(Pair(startPixel.toDouble(), length.toDouble()))
                alter = creator.create(startPixel, length, startPixel + length, startPixel)
                startPixel += length
                if (startPixel >= wrapAt) {
                    startPixel += -wrapAt + wrapTo
                }
            }
            alterPartitions.add(AlterPartition.Partition(alter, boundsList))
        }

        return AlterPartition(alterPartitions)
    }

}

class BlendAlterCreator(
        private val creators: List<AlterCreator>,
        private val timeMultiplierGetter: () -> Double,
) : AlterCreator(CreatorData(
        creators.any { it.creatorData.hasColor },
        creators.any { it.creatorData.hasPattern }
)) {
    override fun create(startPixel: Int, pixelCount: Int, wrapAt: Int, wrapTo: Int): Alter {
        val alters = creators.map { it.create(startPixel, pixelCount, wrapAt, wrapTo) }
        val defaultPercentGetter = ReversingPercentGetter(10.0, 10.0 * 60, 10.0) // by default fade takes 10 seconds
        return AlterBlend(TimeMultiplierPercentGetter(defaultPercentGetter, timeMultiplierGetter), alters)
    }
}

data class CreatorSettings(
        // eventually we will have pixel offset information map here
        val currentPartitionOffset: Int,
        val timeMultiplierGetter: () -> Double
) {
    fun getOffset(offsetString: String): Int? {
        // eventually we may use the pixel offset information map here like the Python version
        return offsetString.toIntOrNull()
    }
}

object Parse {
    private val LOGGER = getLogger()

    private val wordToColorMap = mapOf(
            Pair(listOf("brown"), Color.fromRgb(165, 42, 23)),
            Pair(listOf("deep", "purple"), Color.fromRgb(255, 0, 70)),
            Pair(listOf("hot", "purple"), Color(1.0, 0.0, 0.9642934927623834)),
            Pair(listOf("purple"), Color.PURPLE),
            Pair(listOf("pink"), Color.fromRgb(255, 100, 120)),
            Pair(listOf("red"), Color.RED),
            Pair(listOf("green"), Color.GREEN),
            Pair(listOf("blue"), Color.BLUE),
            Pair(listOf("orange"), Color.fromRgb(255, 45, 0)),
            Pair(listOf("tiger"), Color(1.0, 0.7072935145244612, 0.0)),
            Pair(listOf("yellow"), Color.fromRgb(255, 170, 0)),
            Pair(listOf("teal"), Color.fromRgb(0, 255, 255)),
            Pair(listOf("cyan"), Color.fromRgb(0, 255, 255)),
            Pair(listOf("aqua"), Color.fromRgb(0, 255, 70)),
            Pair(listOf("white"), Color.WHITE),
            Pair(listOf("dupree"), Color.from24Bit(0xFF1100)),
    )

    fun parseColors(text: String): List<Color> {
        val r = mutableListOf<Color>()
        val wordList = text.lowercase().split(Regex("\\s+"))
        for ((index, word) in wordList.withIndex()) {
            if (word.startsWith("#")) {
                var hex = word.substring(1)
                if (hex.length == 3) {
                    hex = hex.toCharArray().joinToString("") { "$it$it" }
                }
                if (hex.length == 6) {
                    val value = hex.toIntOrNull(16)
                    if (value == null) {
                        LOGGER.debug("Could not parse hex: $hex")
                    } else {
                        r.add(Color.from24Bit(value))
                    }
                }
            } else {
                val color = wordToColorMap.entries.firstOrNull { entry ->
                    val startIndex = index - entry.key.size + 1
                    if (startIndex < 0) {
                        false
                    } else {
                        val compareList = wordList.subList(startIndex, index + 1)
                        entry.key.withIndex().all { it.value in compareList[it.index] }
                    }
                }?.value
                if (color != null) {
                    r.add(color)
                }
            }
        }
        return r
    }

    fun getStringAfter(text: String, targetText: String): String? {
        val split = text.split(Regex("\\s+"))
        var previousElement: String? = null
        for (element in split) {
            if (previousElement == targetText) {
                return element
            }
            previousElement = element
        }
        return null
    }

    fun getStringBefore(text: String, targetText: String): String? {
        val split = text.split(Regex("\\s+"))
        var previousElement: String? = null
        for (element in split) {
            if (previousElement != null && element == targetText) {
                return previousElement
            }
            previousElement = element
        }
        return null
    }

    private fun splitTokens(tokens: List<Token>, splitOn: Token): List<List<Token>> {
        val r = mutableListOf<List<Token>>()
        var current = mutableListOf<Token>()
        for (token in tokens) {
            if (token == splitOn) {
                r.add(current)
                current = mutableListOf()
            } else {
                current.add(token)
            }
        }
        r.add(current)
        return r
    }
    fun tokensToCreator(
            tokens: List<Token>,
            textToColorAlter: (String, () -> Double) -> Alter?,
            textToPatternAlter: (String, () -> Double) -> Alter?,
            textToSpeedMultiplier: (String) -> Double?,
            creatorSettings: CreatorSettings,
            useProvidedTimeMultiplier: Boolean = false
    ): AlterCreator? {
        var creatorSettings: CreatorSettings = creatorSettings
        val blendedTokenList = splitTokens(tokens, BLEND_TOKEN)
        if (blendedTokenList.size <= 1) {
            require(blendedTokenList.isNotEmpty())
            val partitionTokenList = splitTokens(blendedTokenList[0], PARTITION_TOKEN)
            if (partitionTokenList.size <= 1) {
                require(partitionTokenList.isNotEmpty())
                val innerTokens = partitionTokenList[0]
                var directlyNestedTimeMultiplier: Double? = null
                for (token in innerTokens) {
                    if (token is StringToken) {
                        val timeMultiplier = textToSpeedMultiplier(token.data)
                        if (timeMultiplier != null) {
                            directlyNestedTimeMultiplier = timeMultiplier
                        }
                        val offsetString = getStringAfter(token.data, "offset")
                        if (offsetString != null) {
                            val newOffset = creatorSettings.getOffset(offsetString)
                            if (newOffset != null) {
                                creatorSettings = creatorSettings.copy(currentPartitionOffset = newOffset)
                            }
                        }
                    }
                }
                val timeMultiplierGetter: () -> Double = if (!useProvidedTimeMultiplier && directlyNestedTimeMultiplier != null) { { directlyNestedTimeMultiplier } } else creatorSettings.timeMultiplierGetter
                val creatorsToCombine = mutableListOf<AlterCreator>()
                for (token in tokens) {
                    when (token) {
                        is StringToken -> {
                            val colorAlter = textToColorAlter(token.data, timeMultiplierGetter)
                            val patternAlter = textToPatternAlter(token.data, timeMultiplierGetter)
                            if (colorAlter != null) {
                                creatorsToCombine.add(StaticCreator(CreatorData(hasColor = true, hasPattern = false), colorAlter))
                            }
                            if (patternAlter != null) {
                                creatorsToCombine.add(StaticCreator(CreatorData(hasColor = false, hasPattern = true), patternAlter))
                            }
                        }
                        is OrganizerToken -> {
                            val creator = tokensToCreator(token.tokens, textToColorAlter, textToPatternAlter, textToSpeedMultiplier, creatorSettings)
                            if (creator != null) {
                                creatorsToCombine.add(creator)
                            }
                        }
                        is StaticToken -> {
                            LOGGER.info("Unknown static token: $token")
                        }
                        is NothingToken -> { }
                        else -> {
                            LOGGER.warn("Unknown token: $token")
                        }
                    }
                }
                val colorCreators = mutableListOf<AlterCreator>()
                val patternCreators = mutableListOf<AlterCreator>()
                for (creator in creatorsToCombine) {
                    if (creator.creatorData.hasColor) {
                        colorCreators.add(creator)
                    } else {
                        patternCreators.add(creator)
                    }
                }
                return CombinerCreator(colorCreators, patternCreators, directlyNestedTimeMultiplier)
            } else {
                // We need to partition
                val creatorsToPartition = mutableListOf<AlterCreator>()
                for (tokenList in partitionTokenList) {
                    val creator = tokensToCreator(tokenList, textToColorAlter, textToPatternAlter, textToSpeedMultiplier, creatorSettings.copy(currentPartitionOffset = 0))
                    creatorsToPartition.add(creator ?: NOTHING_CREATOR)
                }
                return PartitionAlterCreator(creatorsToPartition, creatorSettings.currentPartitionOffset)
            }
        } else {
            val creatorsToBlend = mutableListOf<AlterCreator>()
            for (tokenList in blendedTokenList) {
                val creator = tokensToCreator(tokenList, textToColorAlter, textToPatternAlter, textToSpeedMultiplier, creatorSettings)
                creatorsToBlend.add(creator ?: NOTHING_CREATOR)
            }
            return BlendAlterCreator(creatorsToBlend, creatorSettings.timeMultiplierGetter)
        }
    }

    fun getTimeMultiplier(text: String): Double? {
        val speed = getStringBefore(text, "speed")?.toDoubleOrNull()
        if (speed != null) {
            return speed
        }
        return when {
            "hyper" in text -> 4.0
            "sonic" in text -> 2.0
            "fast" in text -> 1.5
            "medium" in text -> 1.0
            "slow" in text -> 0.5
            "crawl" in text -> 0.25
            "limp" in text -> 0.1
            "still" in text -> 0.001
            "stop" in text -> 0.000000001
            else -> null
        }
    }
}
