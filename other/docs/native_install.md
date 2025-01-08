# Native Install

Notes for doing a native install (without using Docker).
This is not recommended and is not fully supported by me ([@retrodaredevil](https://github.com/retrodaredevil)).

## Install

```shell
curl https://raw.githubusercontent.com/retrodaredevil/led-machine-plus/master/other/scripts/clone_install.sh | sudo bash

# If you need to install java:
sudo apt install openjdk-17-jdk
```

## Configuration

```shell
cd program
sudo ./create_config.sh main
```

Configuration takes place in the `program/configs/main/` directory by default.

## Start Service

```shell
# Start the service
sudo systemctl restart led-machine-main

# Make the service always start when the system boots
sudo systemctl enable led-machine-main
```

## Logs

Useful for debugging:
```shell
sudo journalctl -u led-machine-main.service -f -n100
```

