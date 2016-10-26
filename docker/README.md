
# Docker setup for EDC

## Overview
In this directory are two docker containers that you will need to build and run to simulate a 1-node EDC cluster.

- Consul server `/consul`
- Redis instance with consul client `/redis`

## Building

To build the images, run these docker build commands:

```
docker build -t edc-consul:test consul
docker build -t edc-redis:test redis
```

## Running
To run, the consul server container should be started up first, then the redis instance:

```
docker run -it --name edc-consul edc-consul:test
```

To start the redis container you first have to get the IP of the consul server. 

```
docker inspect edc-consul | grep \"IPAddress\"
```

This will then be provided to the redis container so that it can join the consul cluster.

```
docker run -it -e 'CONSUL_SERVER_ADDRESS={CONSUL_SERVER_IP}' --name edc-redis edc-redis:test
```

At this point you should have the Consul server running and the redis instance registered to Consul. You can check from the Consul UI by going to your browser and hitting `{CONSUL_SERVER_IP}:8500`
