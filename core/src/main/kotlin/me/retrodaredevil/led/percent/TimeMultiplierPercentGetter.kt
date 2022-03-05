package me.retrodaredevil.led.percent

class TimeMultiplierPercentGetter(
        private val percentGetter: PercentGetter,
        private val timeMultiplierGetter: () -> Double
) : PercentGetter {
    override fun getPercent(seconds: Double): Double {
        return percentGetter.getPercent(seconds * timeMultiplierGetter())
    }
}
