package me.retrodaredevil.led.percent

class BouncePercentGetter(
        private val totalPeriod: Double
) : PercentGetter {
    override fun getPercent(seconds: Double): Double {
        var spot = seconds % totalPeriod
        if (spot > totalPeriod / 2.0) {
            spot = totalPeriod - spot
        }
        return spot / totalPeriod * 2
    }
}
