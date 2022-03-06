#!/usr/bin/env sh
BASEDIR=$(dirname "$0")
cd "$BASEDIR" || exit 1

TEMPLATE_FILE="$(cd "$BASEDIR" && pwd)/../other/template/led-machine.service.template"
LED_ROOT=$(cd "$BASEDIR/../" && pwd)

if [ "$#" -eq 1 ]; then
  if ! echo "$1" | grep -Eq "^\w*$"; then
    echo "That is not a valid config directory name!"
    exit 1
  fi
  DIRECTORY="configs/$1"
  mkdir -p "$DIRECTORY" || exit 1
  cd "$DIRECTORY" || exit 1
  touch "config.json" || exit 1
  if id -u led-machine >/dev/null 2>&1; then
    chown led-machine:led-machine "configs/"
    chown led-machine:led-machine "$DIRECTORY"
    chown led-machine:led-machine "$DIRECTORY/config.json"
  fi
  echo "Created directory: $(pwd)"

  DESTINATION_PATH="/etc/systemd/system/led-machine-$1.service"


  echo "led-machine-plus root is $LED_ROOT"
  # Thanks https://stackoverflow.com/a/2705678/5434860
  ESCAPED_LED_ROOT=$(printf '%s\n' "$LED_ROOT" | sed -e 's/[]\/$*.^[]/\\&/g');
  sed -e "s/##name##/$1/g" -e "s/##led_root##/${ESCAPED_LED_ROOT}/" "$TEMPLATE_FILE" >"$DESTINATION_PATH" || exit 1
  echo Added service
  systemctl --no-ask-password daemon-reload || exit 1
  echo "Reloaded systemctl"
else
  echo "Usage: ./create_config.sh <config name>"
fi

