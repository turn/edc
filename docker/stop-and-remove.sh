#!/bin/bash

CONSUL_RUNNING=$(docker inspect --format="{{ .State.Running }}" edc-consul 2> /dev/null)
if [ $? -eq 0 ]; then
  if [ "$CONSUL_RUNNING" == "true" ]; then
	echo "Stopping edc-consul container..."
	docker stop edc-consul
  fi
  echo "Removing edc-consul container..."
  docker rm edc-consul
fi

REDIS_RUNNING=$(docker inspect --format="{{ .State.Running }}" edc-redis 2> /dev/null)
if [ $? -eq 0 ]; then
  if [ "$REDIS_RUNNING" == "true" ]; then
	echo "Stopping edc-redis container..."
	docker stop edc-redis
  fi
  echo "Removing edc-redis container..."
  docker rm edc-redis
fi
