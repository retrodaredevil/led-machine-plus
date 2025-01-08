# Development

This document contains notes useful for the development of LED Machine.
Regular users will find no use of the contents here.

LED Machine Plus is a Kotlin rewrite of [LED Machine](https://github.com/retrodaredevil/led-machine),
which was written in Python.

## Notes

Why Kotlin? Kotlin is a high level language that I enjoy writing code in.
Using something like Python will result in a drop in FPS because of the slowness of floating point operations in Python.
Kotlin won't be as fast as a low level language like C++ or Rust, but it is not the bottleneck for the FPS of the LED strip.

## Building
```shell
./gradlew shadowJar
scp app/build/libs/app-all.jar ledpi:/opt/led-machine-plus/program/led-machine-plus.jar
```

## Timing and FPS/Refresh Rate

The [rpi_ws281x](https://github.com/jgarff/rpi_ws281x) library has the implementation
that pretty much all other implementations reference.

* The wait time between renders is this equation (1.25 µs per bit)
  * Source: https://github.com/jgarff/rpi_ws281x/blob/7fc0bf8b31d715bbecf28e852ede5aaa388180da/ws2811.c#L1226
  * wait_microseconds = channel_count * led_count * 8 * 1.25 + 300
  * Channel count is normally 3, so to get the FPS do this:
    * 1000000 / (led_count * 3 * 8 * 1.25 + 300)
    * If you have 1200 LEDs, the FPS will be 27.5 fps
    * If you have 300 LEDs, the FPS will be 107.5 fps

[WS281xSpi.java](https://github.com/mattjlewis/diozero/blob/main/diozero-ws281x-java/src/main/java/com/diozero/ws281xj/spi/WS281xSpi.java) 
([permalink](https://github.com/mattjlewis/diozero/blob/50aac1f13aa1195471f8726affe5365c9403d485/diozero-ws281x-java/src/main/java/com/diozero/ws281xj/spi/WS281xSpi.java#L1))
contains the implementation we use when dealing with WS281x control through SPI and mimics the timing of the rpi_ws281x library.

[Understanding the WS2812 (2014)](https://cpldcpu.com/2014/01/14/light_ws2812-library-v2-0-part-i-understanding-the-ws2812/)
states that each bit must not take less than 1.25 microseconds, so it doesn't look like we can make any improvement in the timing here.

The conclusion here is that ws281x LEDs have a slow refresh rate compared to alternatives,
and the refresh rate worsens as you add more LEDs.

## TODO
* Choose direction -- no reversing


## New Language Ideas

3 layers
* pixel layer - interacts with pixels directly
* positional layer - interacts with positions
* gradient layer - a gradient has a certain color when given a position between 0% and 100%


Positional layer nodes and examples
* speed - `speed 5 : <positional node>`
  * The nested positional node is now moving at 5 units per second
* length - `length 100 : (blue red)`
  * The nested gradient is spread and repeated over 100 units
* round - `round 100 : (blue red)`
  * Similar to `length` - the nested gradient is spread and repeated over about 100 units. 100 is rounded to the nearest length that fits evenly into the LED strip
* divide - `divide 5 : (blue red)`
  * The nested gradient is spread and repeated 5 times over the length of the LED strip

Gradient layer nodes and examples
* period - `period 10 : (yellow green)`
  * The nested gradient moves and completes a cycle in 10 seconds
* mirror - `mirror : period 10 : (yellow green)`
  * The nested gradient is mirrored so that yellow green now moves toward the center of the gradient



Keywords
* Color keywords - each of these keywords defines a gradient that is all that color
  * `red`
  * `green`
  * `blue`
  * `brown`
  * `deep purple`
  * `hot purple`
  * `purple`
  * `pink`
  * `orange`
  * `tiger`
  * `yellow`
  * `teal`
  * `cyan`
  * `aqua`
  * `white`
  * `dupree`
* Color literal
  * `#FFF` - 12 bit color definition
    * Alternatively, `FFF` is also an example of a 12 bit color definition
  * `#FFFFFF` - 24 bit color definition
    * Alternatively, `FFFFFF` is also an example of a 12 bit color definition
