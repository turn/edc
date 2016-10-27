FROM java:8

ARG MIRROR=http://apache.osuosl.org
ARG VERSION=3.4.9

RUN  apt-get update
RUN  apt-get install -y curl

RUN  cd /opt \
     && curl $MIRROR/zookeeper/zookeeper-$VERSION/zookeeper-$VERSION.tar.gz | tar xz \
     && mv /opt/zookeeper-$VERSION /opt/zookeeper \
     && cp /opt/zookeeper/conf/zoo_sample.cfg /opt/zookeeper/conf/zoo.cfg \
     && mkdir /tmp/zookeeper

EXPOSE 2181

WORKDIR /opt/zookeeper

VOLUME = ["opt/zookeeper/conf", "/tmp/zookeeper"]

ENTRYPOINT ["/opt/zookeeper/bin/zkServer.sh"]

CMD ["start-foreground"]
