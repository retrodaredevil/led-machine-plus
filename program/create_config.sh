#!/usr/bin/env sh
BASEDIR=$(dirname "$0")
cd "$BASEDIR" || exit 1

if [ "$#" -eq 1 ]; then
  if ! echo "$1" | grep -Eq "^\w*$"; then
    echo "That is not a valid config directory name!"
    exit 1
  fi
  DIRECTORY="configs/$1"
  mkdir -p "$DIRECTORY" || exit 1
  cd "$DIRECTORY" || exit 1
  touch "config.json" || exit 1
  echo "Created directory: $(pwd)"
else
  echo "Usage: ./create_config.sh <config name>"
fi
