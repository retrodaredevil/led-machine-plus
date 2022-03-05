package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.round

private class Twinkle(
        private val peakPointSeconds: Double,
        private val fadeDurationSeconds: Double
) {
    fun getBrightness(seconds: Double): Double {
        val distance = (seconds - peakPointSeconds).absoluteValue
        return max(0.0, 1 - distance / fadeDurationSeconds)
    }
    fun isStale(seconds: Double): Boolean {
        return peakPointSeconds + fadeDurationSeconds < seconds
    }
}

private const val SECTION_LENGTH = 20

class AlterTwinkle(
        private val numberOfPixels: Int,
        private val minPercentToLightUp: Double,
        private val maxPercentToLightUp: Double,
) : Alter {

    private var lastUpdate: Double? = null
    private val twinkleMap = mutableMapOf<Int, MutableList<Twinkle>>()

    private fun randomize(currentSeconds: Double) {
        val numberOfSections = ceil(numberOfPixels / SECTION_LENGTH.toDouble()).toInt()

        val relativeIndexList = (0 until SECTION_LENGTH).toMutableList()
        for (sectionIndex in 0 until numberOfSections) {
            val pixelIndexStart = sectionIndex * SECTION_LENGTH
            relativeIndexList.shuffle()
            val numberToLightUp = (round(minPercentToLightUp * SECTION_LENGTH).toInt()..round(maxPercentToLightUp * SECTION_LENGTH).toInt()).random()

            for (pixelIndexOffset in relativeIndexList.subList(0, numberToLightUp)) {
                val pixelIndex = pixelIndexStart + pixelIndexOffset
                val timeOffset = 0.5 + Math.random()
                val peakPointSeconds = currentSeconds + timeOffset
                twinkleMap.computeIfAbsent(pixelIndex) { mutableListOf() }.add(Twinkle(peakPointSeconds, 0.3))
            }
        }
    }

    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        val lastUpdate = this.lastUpdate
        if (lastUpdate == null || lastUpdate > seconds || lastUpdate + 1.0 < seconds) {
            this.lastUpdate = seconds
            randomize(seconds)
        }
        if (currentColor == null) {
            return null
        }
        // In the future, we could figure out a way to not just cast this to an int
        val key = pixelPosition.toInt()
        val twinkleList = twinkleMap[key] ?: return Color.BLACK

        val iter = twinkleList.iterator()
        var maxBrightness = 0.0
        while (iter.hasNext()) {
            val twinkle = iter.next()
            if (twinkle.isStale(seconds)) {
                iter.remove()
            }
            val brightness = twinkle.getBrightness(seconds)
            maxBrightness = max(maxBrightness, brightness)
        }

        return currentColor.scale(maxBrightness)
    }
}