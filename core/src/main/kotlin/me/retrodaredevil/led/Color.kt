package me.retrodaredevil.led

import kotlin.math.roundToInt


open class RawColor(
        val r: Double,
        val g: Double,
        val b: Double,
) {
    operator fun times(multiplier: Double): RawColor {
        return RawColor(r * multiplier, g * multiplier, b * multiplier)
    }
    operator fun plus(other: RawColor): RawColor {
        return RawColor(r + other.r, g + other.g, b + other.b)
    }

    override fun toString(): String {
        return "RawColor(r=$r, g=$g, b=$b)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RawColor) return false

        if (r != other.r) return false
        if (g != other.g) return false
        if (b != other.b) return false

        return true
    }

    override fun hashCode(): Int {
        var result = r.hashCode()
        result = 31 * result + g.hashCode()
        result = 31 * result + b.hashCode()
        return result
    }


}

class Color(
        r: Double,
        g: Double,
        b: Double,
) : RawColor(r, g, b) {
    init {
        check(r in 0.0..1.0) { "r is out of range! r: $r" }
        check(g in 0.0..1.0) { "g is out of range! g: $g" }
        check(b in 0.0..1.0) { "b is out of range! b: $b" }
    }
    fun scale(scalar: Double): Color {
        check(scalar >= 0) { "scalar cannot be negative! scalar: $scalar" }
        check(scalar <= 1) { "scalar cannot be > 1! scalar: $scalar" }
        return Color(r * scalar, g * scalar, b * scalar)
    }

    override fun toString(): String {
        return "Color(r=$r, g=$g, b=$b)"
    }

    fun to24Bit(): Int {
        val rawR = (r * 255).roundToInt()
        val rawG = (g * 255).roundToInt()
        val rawB = (b * 255).roundToInt()
        return (rawR shl 16) or (rawG shl 8) or rawB
    }
}