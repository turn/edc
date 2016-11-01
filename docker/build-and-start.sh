#!/bin/bash

DOCKER_DIR=$1

echo "Building edc-consul..."
docker build -t edc-consul:test "$DOCKER_DIR"/consul

echo "Building edc-redis..."
docker build -t edc-redis:test "$DOCKER_DIR"/redis

CONSUL_RUNNING=$(docker inspect --format="{{ .State.Running }}" edc-consul 2> /dev/null)

if [ $? -eq 0 ]; then
  if [ "$CONSUL_RUNNING" == "true" ]; then
	echo "Stopping edc-consul container..."
	docker stop edc-consul
  fi
  echo "Removing edc-consul container..."
  docker rm edc-consul
fi

echo "Running edc-consul container..."
docker run -d --name edc-consul edc-consul:test

REDIS_RUNNING=$(docker inspect --format="{{ .State.Running }}" edc-redis 2> /dev/null)

if [ $? -eq 0 ]; then
  if [ "$REDIS_RUNNING" == "true" ]; then
	echo "Stopping edc-redis container..."
	docker stop edc-redis
  fi
  echo "Removing edc-redis container..."
  docker rm edc-redis
fi

echo "Running edc-redis container..."
docker run -d --link edc-consul --name edc-redis edc-redis:test
