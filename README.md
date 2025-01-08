# LED Machine Plus

LED Machine Plus controls an LED strip by sending messages in a Discord channel.
The message you send configures the color, speed, and pattern displayed on the LED strip.

This can be configured to poll messages from a Discord channel. Those messages control WS281x LEDs over a GPIO pin.

The contents of this readme will help you understand what you need to have and to do to set up LED Machine!


## Features

* Runs in an unprivileged Docker container
* Create custom patterns by simply listing the colors you want to be blended together
* HEX color code support (#RRGGBB or #RGB syntax)
* Discord bot reacts to your messages so you know they were received
* "Fade off 10 minutes" - fades off over 10 minutes


## Prerequisites

* WS281x LED strip
* A Raspberry Pi device, or any device that supports the SPI protocol through its GPIO
* Your device should have an up-to-date version of its operating system, preferably DietPi
* Docker should be Installed on your device


## Hardware Setup

* Connect
  * Data wires
    * LED strip's ground to your device's ground
    * LED strip's data wire to the SPI 0 MOSI pin ([GPIO 10 / pin 19](https://pinout.xyz/pinout/pin19_gpio10/) on Raspberry Pi Devices)
      * Alternatively, on Raspberry Pi devices (but not an RPi 5) you may use [GPIO 18 / pin 12](https://pinout.xyz/pinout/pin12_gpio18/) and don't enable SPI
  * Power to your LED strip (do not use your device's 5v power to power your LEDs) - Get an external 5v (or 12v depending on your LED strip) power supply
    * Connect LED strip's positive to power supply's positive
    * Connect LED strip's ground to power supply's ground

Use `raspi-config` or `dietpi-config` to enable SPI on your device.


## LED Machine Setup

Run these commands to start your setup:

```shell
sudo mkdir -p /opt/containers/led-machine/data
sudo chown -R 2000:2000 /opt/containers/led-machine

mkdir ~/Documents/led-machine
cd ~/Documents/led-machine
# Note the result of this command:
getent group spi
# Create and edit docker-compose.yml
nano docker-compose.yml
```

Paste this into your docker-compose.yml file:

```yaml
services:
  led-machine:
    image: ghcr.io/retrodaredevil/led-machine:edge
    container_name: led-machine
    user: 2000:2000
    group_add:
      - THE RESULT OF THE getend group spi COMMAND HERE
    devices:
      - /dev/spidev0.0
    command: /app/config/config.json
    working_dir: /app/data  # The save file (saved.json) is saved in the working directory
    volumes:
      - ./config:/app/config:ro
      - /opt/containers/led-machine/data:/app/data  # Optional, but recommended
      - /opt/containers/led-machine/jvmlog:/app/jvmlog  # Optional
    restart: unless-stopped
```

Now we want to create our configuration file:

```shell
mkdir config
nano config/config.json
```

Paste this into your `config.json` file that you are now editing:

```json
{
  "message_config": {
    "type": "discord",
    "bot_token": "secret",
    "channel_id": 123
  },
  "led_count": 50,
  "gpio": 99,
  "start_skip": 0,
  "spi": true,
  "order": "RGB"
}
```

Edit the contents of `config.json` accordingly. Replace `secret` with your Discord bot token and replace `123` with your channel ID (skip to the section below about setting up a Discord bot if necessary).
Replace `50` with your LED count.
Replace `99` with the GPIO pin you are using.
Optionally set `spi` to false if you are not using SPI.

Now run this to start your container:

```shell
docker compose up -d
```

To check to make sure everything is successful, we can check the logs:

```shell
docker compose logs
```

Additionally, we can make sure that the service is not in a crash loop:

```console
lavender@ledpi:~/Documents/led-machine$ docker compose ps
NAME          IMAGE                                     COMMAND                  SERVICE       CREATED        STATUS         PORTS
led-machine   ghcr.io/retrodaredevil/led-machine:edge   "java -XX:ErrorFile=…"   led-machine   2 minutes ago  Up 30 seconds
```

You can see that with the above output, the container has been up for 30 seconds, which means enough time has passed for us to be confident that it's working!

Now send some messages in your Discord channel. Try `rainbow`.
If LED Machine reacts to your message with a heart, then you configured your bot correctly.
Additionally, if the LEDs turn on, it's working as expected.


## Discord App Setup
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
  * 

### Using SPI and non-root user
If your LED strip has 4 wires (2 data wires), then you should control it using SPI.
This does not require root. All SPI configurations are hardcoded to use CE0 (pin 24).

You can also use SPI with 1 data wire, but I found this to be unreliable on a Raspberry Pi.
SPI is however reliable on other SBCs (I have tested SPI on an Orange Pi One and it works well).

If your LED strip has two data wires, connect them accordingly (I'm not sure how to connect them).
If your LED strip only has one data wire, connect that data wire to either SPI0 MOSI, or SPI1 MOSI.
Pinout can be found at https://pinout.xyz/pinout/spi.


## Additional Notes

Note that SPI is required for non-Raspberry Pi devices (as ws281x library only supports PWM control for Raspberry Pi devices 1, 2, 3 and 4).
Additionally, (as of 2025-01-05), Raspberry Pi 5s must also use SPI.

### Common Errors

* You need to enable SPI if:
  * `open failed: No such file or directorySPI message transfer failed: Bad file descriptorSPI message transfer failed: Bad file descriptor`

