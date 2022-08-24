package me.retrodaredevil.led.config

import com.fasterxml.jackson.annotation.JsonProperty

class BaseConfig(
        @JsonProperty("message_config")
        @get:JsonProperty("message_config")
        val messageConfig: MessageConfig,

        @JsonProperty("led_count")
        @get:JsonProperty("led_count")
        val ledCount: Int,

        @JsonProperty("gpio")
        @get:JsonProperty("gpio")
        val gpioPort: Int,

        @JsonProperty("start_skip")
        @get:JsonProperty("start_skip")
        val startPixelSkipCount: Int = 0,

        @JsonProperty("end_skip")
        @get:JsonProperty("end_skip")
        val endPixelSkipCount: Int = 0,

        val spi: Boolean = false,

        val order: ColorOrder = ColorOrder.GRB
) {
}

enum class ColorOrder {
    GRB, // used by many WS2812s
    RGB, // used by many WS2815s
}
