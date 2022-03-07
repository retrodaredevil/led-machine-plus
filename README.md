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

Run `sudo raspi-config`. Go to "Interface Options" and enable SPI.
