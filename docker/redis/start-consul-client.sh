#!/bin/sh
set -e

# Bind to docker bridge interface
CONSUL_BIND_INTERFACE="eth0"
CONSUL_BIND_ADDRESS=$(ip -o -4 addr list $CONSUL_BIND_INTERFACE | head -n1 | awk '{print $4}' | cut -d/ -f1)
if [ -z "$CONSUL_BIND_ADDRESS" ]; then
  echo "Could not find IP for interface '$CONSUL_BIND_INTERFACE', exiting"
  exit 1
fi

CONSUL_BIND="-bind=$CONSUL_BIND_ADDRESS"
echo "==> Found address '$CONSUL_BIND_ADDRESS' for interface '$CONSUL_BIND_INTERFACE', setting bind option..."

# Consul server address
#if [ -z "$CONSUL_SERVER_ADDRESS" ]; then
#    echo "Consul server address is not provided, exiting"
#    exit 1
#fi
#CONSUL_SERVER="-retry-join=$CONSUL_SERVER_ADDRESS"
CONSUL_SERVER="-retry-join=edc-consul"

exec consul agent \
        $CONSUL_SERVER \
        $CONSUL_BIND \
        "$@"



