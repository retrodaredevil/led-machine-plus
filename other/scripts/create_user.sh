#!/usr/bin/env sh

# This has to be run with root

# Add group
if ! grep -q led-machine /etc/group >/dev/null 2>&1; then
  groupadd led-machine
  if ! grep -q led-machine /etc/group >/dev/null 2>&1; then
    echo Unable to create led-machine group
    exit 1
  fi
fi

IMPORTANT_GROUPS=input,dialout,video,kmem
UNIMPORTANT_GROUPS=gpio,i2c,spi


# Add user
if ! id -u led-machine >/dev/null 2>&1; then
  useradd -r -g led-machine -G "$UNIMPORTANT_GROUPS,$IMPORTANT_GROUPS" led-machine 2>/dev/null
  useradd -r -g led-machine -G "$IMPORTANT_GROUPS" led-machine 2>/dev/null
  if ! id -u led-machine >/dev/null 2>&1; then
    echo Unable to create user
    exit 1
  fi
  passwd -l led-machine || (echo Could not lock led-machine passwd; exit 1)
fi
# Add user to groups
usermod -a -G "$IMPORTANT_GROUPS" led-machine || exit 1 # add groups to user just to make sure they have all needed groups
usermod -a -G "$UNIMPORTANT_GROUPS" led-machine 2>/dev/null || echo "On a system that does not support gpio,i2c,spi groups"

echo Created \"led-machine\" user and group.
echo You can add a user to the group with \"adduser \<USER NAME HERE\> led-machine\".
echo You need to log out and log back in after adding groups.
