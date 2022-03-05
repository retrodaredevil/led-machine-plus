package me.retrodaredevil.led.percent

fun interface PercentGetter {
    fun getPercent(seconds: Double): Double
}
