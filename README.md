# LED Machine Plus
LED Machine Plus is a Kotlin rewrite of [LED Machine](https://github.com/retrodaredevil/led-machine),
which was written in Python.

This can be configured to poll messages from a Slack channel. Those messages control WS281x LEDs over a GPIO pin.

### Notes

Why Kotlin? Kotlin is a high level language that I enjoy writing code in.
Using something like Python will result in a drop in FPS because of the slowness of floating point operations in Python.
Kotlin won't be as fast as a low level language like C++ or Rust, but it is not the bottleneck for the FPS of the LED strip.

* The wait time between renders is this equation (1.25 µs per bit)
  * Source: https://github.com/jgarff/rpi_ws281x/blob/7fc0bf8b31d715bbecf28e852ede5aaa388180da/ws2811.c#L1226
  * wait_microseconds = channel_count * led_count * 8 * 1.25 + 300
  * Channel count is normally 3, so to get the FPS do this:
    * 1000000 / (led_count * 3 * 8 * 1.25 + 300)
    * If you have 1200 LEDs, the FPS will be 27.5 fps
    * If you have 300 LEDs, the FPS will be 107.5 fps

### Installing
```shell
curl https://raw.githubusercontent.com/retrodaredevil/led-machine-plus/master/other/scripts/clone_install.sh | sudo bash

# If you need to install java:
sudo apt install openjdk-11-jdk openjdk-11-jdk-headless
```

### Configuration
First you should create a configuration.

```shell
cd program
sudo ./create_config.sh main
```

Configuration takes place in the `program/configs/main/` directory by default.

It is recommended to use pin 12 (GPIO 18), as that is the only pin that is known to work.

### Using SPI and non-root user
If your LED strip has 4 wires (2 data wires), then you should control it using SPI.
This does not require root. All SPI configurations are hardcoded to use CE0 (pin 24).

You can also use SPI with 1 data wire, but I found this to be unreliable on a Raspberry Pi.
SPI is however reliable on other SBCs (I have tested SPI on an Orange Pi One and it works well).

If your LED strip has two data wires, connect them accordingly (I'm not sure how to connect them).
If your LED strip only has one data wire, connect that data wire to either SPI0 MOSI, or SPI1 MOSI.
Pinout can be found at https://pinout.xyz/pinout/spi.

Run `sudo raspi-config`. Go to "Interface Options" and enable SPI.

Note that SPI is required for non-Raspberry Pi devices (as ws281x library only supports PWM control for Raspberry Pi devices 1, 2, 3 and 4).
Additionally, (as of 2025-01-05), Raspberry Pi 5s must also use SPI.

### Building yourself
```shell
./gradlew shadowJar
scp app/build/libs/app-all.jar ledpi:/opt/led-machine-plus/program/led-machine-plus.jar
```

### Using an Orange Pi One
If you want to use an Orange Pi One, connect ground to ground, and your data wire to
pin 19 (MOSI.0). When configuring, set `"spi": true`. Note: pin 40 is closest to Ethernet on Orange Pi One.
You can find pinout [here](https://www.instructables.com/Orange-Pi-One-Python-GPIO-basic/).

You will need to enable SPI on your Orange Pi.

### TODO
* Choose directijon -- no reversing
* Use manifest file for slack setup

### Other stuff
Useful to prefer WiFi for internet:
```
interface eth0
metric 300

interface wlan0
metric 200
```

Useful for debugging:
```shell
sudo journalctl -u led-machine-main.service -f -n100
```

Useful for copying jar:
```shell
./gradlew app:jar
rsync app/build/libs/app-0.0.1.jar pi@192.168.1.151:/opt/led-machine-plus/program/led-machine-plus.jar
```

### Slack App Setup
* App level token needs to have `connections:write` scope. This token will be pasted into the `app_token` property.
* Enable Socket Mode
* Go to "OAuth & Permissions" 
  * Add `channels:history` (most important), `groups:history`, `im:history`, as Bot Token Scopes
    * Optionally add these for future use: `channels:write`, `reactions:write`, `reactions:read`, `chat:write`, `channels:read`
  * Install to your workspace
  * Get the Bot User OAuth Token that will be pasted into the `bot_token` property.
* Go to "Event Subscriptions" and Enable Events
  * Subscribe to bot events: `message.channels`, `message.groups`, `message.im`
* Add your bot to the channel you want it to have access to

### Discord App Setup
* Create application at https://discord.com/developers/applications
  * Go to Bot
    * Create a bot
    * Create "Reset Token", confirm, and copy the newly generated token. You will need this in your configuration
    * Enable "Message Content Intent"
  * Go to OAuth2 > URL Generator
    * Add scope "bot"
      * Add permission: "Read Messages/View Channels", "Add Reactions"
    * Click on the generated URL and add the application to your server
* Enable developer mode in your discord client
  * Right-click on the channel you want to use and click "copy id".

### Common Errors

* You need to enable SPI if:
  * `open failed: No such file or directorySPI message transfer failed: Bad file descriptorSPI message transfer failed: Bad file descriptor`

### New Language Ideas

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
