#!/usr/bin/env sh

BASEDIR=$(dirname "$0")
#cd "$BASEDIR" || exit 1

BASE_CONFIG=$1
if [ -z "$BASE_CONFIG" ] || [ "$BASE_CONFIG" = " " ]; then
  BASE_CONFIG="config.json"
fi
echo Using base config = "$BASE_CONFIG"

java -jar "$BASEDIR/../../program/led-machine-plus.jar" "$BASE_CONFIG"

