
# Docker setup for EDC

## TL;DR
Run `build-and-start.sh` to build all containers and start them.

When you're done, run `stop-and-remove.sh` to stop all containers and clean them up.

## Detailed setup

In this directory are two docker containers that you will need to build and run to simulate a 1-node EDC cluster.

- Consul server `/consul`
- Redis instance with consul client `/redis`

### Building

To build the images, run these docker build commands:

```
docker build -t edc-consul:test consul
docker build -t edc-redis:test redis
```

### Running
To run, the consul server container should be started up first, then the redis instance:

```
docker run -d --name edc-consul edc-consul:test
```

Next, run the redis instance, linking the consul server:

```
docker run -d --link edc-consul --name edc-redis edc-redis:test
```

At this point you should have the Consul server running and the redis instance registered to Consul. You can check from the Consul UI by going to your browser and hitting `{CONSUL_SERVER_IP}:8500`.

You can get the Consul IP address by using the docker `inspect` command: 

```
docker inspect --format="{{ .NetworkSettings.IPAddress }}" edc-consul
```
