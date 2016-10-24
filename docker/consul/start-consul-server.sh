#!/bin/sh
set -e

# Bind to docker bridge via eth0 interface
CONSUL_BIND_INTERFACE="eth0"
CONSUL_BIND_ADDRESS=$(ip -o -4 addr list $CONSUL_BIND_INTERFACE | head -n1 | awk '{print $4}' | cut -d/ -f1)
if [ -z "$CONSUL_BIND_ADDRESS" ]; then
  echo "Could not find IP for interface '$CONSUL_BIND_INTERFACE', exiting"
  exit 1
fi

# Use for internal and client communication
CONSUL_BIND="-bind=$CONSUL_BIND_ADDRESS -client=$CONSUL_BIND_ADDRESS"
echo "==> Found address '$CONSUL_BIND_ADDRESS' for interface '$CONSUL_BIND_INTERFACE', setting bind option..."

exec consul agent -ui $CONSUL_BIND "$@"
