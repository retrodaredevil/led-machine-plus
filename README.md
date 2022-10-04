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
