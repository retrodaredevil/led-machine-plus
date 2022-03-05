package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color

typealias Position = Double

class LedMetadata {

}

interface Alter {
    fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color?
}