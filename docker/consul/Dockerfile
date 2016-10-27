FROM ubuntu:16.04

# Consul version
ENV CONSUL_VERSION=0.7.0

# Base stuff
RUN \
  apt-get update && \
  apt-get install -y wget unzip iproute2

# Install Consul
RUN \
    useradd consul && \
    mkdir -p /tmp/build && \
    cd /tmp/build && \
    wget https://releases.hashicorp.com/consul/${CONSUL_VERSION}/consul_${CONSUL_VERSION}_linux_amd64.zip && \
    unzip -d /bin consul_${CONSUL_VERSION}_linux_amd64.zip && \
    cd /tmp && \
    rm -rf /tmp/build && \
    mkdir -p /consul/data && \
    chown -R consul:consul /consul && \
    mkdir -p /etc/consul.d

# Copy Consul configuration files
COPY consul.conf.json /etc/consul.d/consul.conf.json

# Expose the consul data directory as a volume since there's mutable state in there.
VOLUME /consul/data

# Expose all Consul ports
EXPOSE 8300 8301 8301/udp 8302 8302/udp 8400 8500 8600 8600/udp

# Copy startup script
COPY start-consul-server.sh /usr/local/bin/start-consul-server.sh

# Run startup script
CMD ["/usr/local/bin/start-consul-server.sh", "-data-dir=/consul/data", "-config-dir=/etc/consul.d"]