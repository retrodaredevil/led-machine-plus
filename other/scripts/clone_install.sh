#!/usr/bin/env sh

# This script is meant to be executed by someone who has not yet cloned the led-machine-plus repo.
#   This script can be directly downloaded over github's raw download link.

cd /opt || exit 1
if ! type git 1>/dev/null 2>/dev/null; then
  if ! type apt-get 1>/dev/null 2>/dev/null; then
    echo "Git is not installed, and this system does not have apt-get, so install git on your own then rerun this script"
    exit 1
  fi
  echo "Git is not installed, so we will try to install it now"
  apt-get update || exit 1
  apt-get install git || exit 1
fi
if [ -d led-machine-plus/ ]; then
  cd led-machine-plus || exit 1
  echo "Found led-machine-plus directory. Going to try to pull..."
  git pull || exit 1
else
  git clone https://github.com/retrodaredevil/led-machine-plus.git || exit 1
  cd led-machine-plus || exit 1
fi

# TODO move some of this to an install script

other/scripts/create_user.sh || exit 1
other/scripts/update_perms.sh continue || exit 1
chmod g+w /dev/mem

