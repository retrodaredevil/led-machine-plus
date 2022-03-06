# LED Machine Plus
LED Machine Plus is a Kotlin rewrite of [LED Machine](https://github.com/retrodaredevil/led-machine),
which was written in Python.

This can be configured to poll messages from a Slack channel. Those messages control WS281x LEDs over a GPIO pin.

### Installing
```shell
curl https://raw.githubusercontent.com/retrodaredevil/led-machine-plus/master/other/scripts/clone_install.sh | sudo bash
```

### Configuration
Configuration takes place in the `program/configs/main/` directory by default.
