FROM ubuntu:16.04

# Base stuff
RUN \
  apt-get update && \
  apt-get install -y wget unzip build-essential supervisor iproute2

## Consul

# Install Consul
RUN \
  useradd consul && \
  cd /tmp && \
  wget https://releases.hashicorp.com/consul/0.7.0/consul_0.7.0_linux_amd64.zip && \
  unzip consul_0.7.0_linux_amd64.zip && \
  cp -f consul /usr/local/bin && \
  mkdir -p /data/consul && \
  chown -R consul:consul /data/consul && \
  mkdir -p /etc/consul.d

COPY start-consul-client.sh /usr/local/bin/start-consul-client.sh

# Expose Consul ports
EXPOSE 8300 8301 8301/udp 8302 8302/udp 8400 8500 8600 8600/udp

## Redis

# Install Redis.
RUN \
  cd /tmp && \
  wget http://download.redis.io/redis-stable.tar.gz && \
  tar xvzf redis-stable.tar.gz && \
  cd redis-stable && \
  make && \
  make install && \
  cp -f src/redis-sentinel /usr/local/bin && \
  mkdir -p /etc/redis && \
  cp -f *.conf /etc/redis && \
  rm -rf /tmp/redis-stable* && \
  sed -i 's/^\(bind .*\)$/# \1/' /etc/redis/redis.conf && \
  sed -i 's/^\(dir .*\)$/# \1\ndir \/data\/redis/' /etc/redis/redis.conf && \
  sed -i 's/^\(protected-mode .*\)$/# \1\nprotected-mode no/' /etc/redis/redis.conf && \
  sed -i 's/^\(logfile .*\)$/# \1/' /etc/redis/redis.conf && \
  mkdir -p /data/redis

# Copy redis service consul configuration file
COPY redis-consul.conf.json /etc/consul.d/redis-consul.conf.json

# Expose redis ports
EXPOSE 6379

## supervisord

RUN \
  mkdir -p /var/log/supervisor

COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# supervisor is startup command
CMD ["/usr/bin/supervisord"]

# Define mountable directories.
VOLUME ["/data"]

# Define working directory.
WORKDIR /data