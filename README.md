# LED Machine Plus
LED Machine Plus is a Kotlin rewrite of [LED Machine](https://github.com/retrodaredevil/led-machine),
which was written in Python.

This can be configured to poll messages from a Slack channel. Those messages control WS281x LEDs over a GPIO pin.

### Installing
```shell
curl https://raw.githubusercontent.com/retrodaredevil/led-machine-plus/master/other/scripts/clone_install.sh | sudo bash
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
SPI is however reliable on other SBCs (I have tested SPI on an Orange Pi One and it works well)

Run `sudo raspi-config`. Go to "Interface Options" and enable SPI.

### Building yourself
```shell
./gradlew app:jar
```

### Using an Orange Pi One
If you want to use an Orange Pi One, connect ground to ground, and your data wire to
pin 19 (MOSI.0). When configuring, set `"spi": true`. Note: pin 40 is closest to Ethernet on Orange Pi One.
You can find pinout [here](https://www.instructables.com/Orange-Pi-One-Python-GPIO-basic/).

You will need to enable SPI on your Orange Pi.

### TODO
* Choose directijon -- no reversing

### Other stuff
Useful to prefer WiFi for internet:
```
interface eth0
metric 300

interface wlan0
metric 200
```
