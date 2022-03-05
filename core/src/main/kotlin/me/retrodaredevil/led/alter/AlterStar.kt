package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color
import kotlin.math.max

class AlterStar(
        expectedPixels: Int,
        padding: Int,
        private val timeMultiplierGetter: () -> Double,
        private val reverse: Boolean = false,
) : Alter {
    private val spawnLower = -padding
    private val spawnUpper = expectedPixels + padding
    private val stars = mutableListOf<Star>()
    private var lastTimestamp: Double? = null

    init {
        val totalDistance = expectedPixels + padding * 2
        val totalStars = (totalDistance * STAR_PER_PIXEL).toInt()
        for (i in 0 until totalStars) {
            val star = Star()
            stars.add(star)
            star.position = (spawnLower..spawnUpper).random().toDouble()
            star.velocity = ((0..1).random() * 2 - 1) * (0.3 + (1.5 - 0.3) * Math.random())
            if (reverse) {
                star.thickness = 2.0
            } else {
                star.brightness = 0.2 + Math.random() * 0.6
            }
            star.brightnessLeft = star.brightness
            star.brightnessRight = star.brightness
        }

        val shootingStar = Star()
        stars.add(shootingStar)
        shootingStar.apply {
            thickness = 1.0
            fadeDistanceRight = 4.0
            fadeDistanceLeft = 1.0
            brightnessRight = 0.1
            velocity = -10.0
        }
    }

    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        val lastTimestamp = lastTimestamp
        var delta = 0.0
        if (lastTimestamp != null) {
            delta = seconds - lastTimestamp
        }
        this.lastTimestamp = seconds

        if (delta > 0) {
            for (star in stars) {
                star.position += star.velocity * delta * timeMultiplierGetter()
                if (star.position > spawnUpper) {
                    star.position = spawnLower + (star.position - spawnUpper)
                } else if (star.position < spawnLower) {
                    star.position = spawnUpper - (spawnLower - star.position)
                }
            }
        }
        if (currentColor == null) {
            return null
        }
        var brightness = 0.0
        for (star in stars) {
            val lower = star.position - star.thickness / 2.0
            val upper = star.position + star.thickness / 2.0
            when (pixelPosition) {
                in lower..upper -> {
                    brightness = max(brightness, star.brightness)
                }
                in (lower - star.fadeDistanceLeft)..lower -> {
                    brightness = max(brightness, (pixelPosition - (lower - star.fadeDistanceLeft)) / star.fadeDistanceLeft * star.brightnessLeft)
                }
                in upper..(upper + star.fadeDistanceRight) -> {
                    brightness = max(brightness, ((upper + star.fadeDistanceRight) - pixelPosition) / star.fadeDistanceRight * star.brightnessRight)
                }
            }
        }
        check(brightness in 0.0..1.0) { "Brightness is $brightness" }
        return currentColor.scale(if (reverse) 1.0 - brightness else brightness)
    }

    private class Star {
        var position = 0.0
        var velocity = 0.0
        var brightness = 1.0
        var thickness = 0.0
        var fadeDistanceLeft = 1.5
        var fadeDistanceRight = 1.5
        var brightnessLeft = 0.9
        var brightnessRight = 0.9
    }
    companion object {
        private val STAR_PER_PIXEL = 1 / 12.0
    }
}
