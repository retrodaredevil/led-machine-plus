#!/usr/bin/env sh
BASEDIR=$(dirname "$0")

if [ "$#" -eq 1 ]; then
  RELATIVE_PATH=$(realpath --relative-to="$BASEDIR/../../program/" "$1")
  ln -sf "$RELATIVE_PATH" "$BASEDIR/../../program/led-machine-plus.jar" || exit 1
  echo "Made led-machine-plus.jar reference $1"
else
  echo "Usage: ./use_jar.sh <path to jar>"
fi

