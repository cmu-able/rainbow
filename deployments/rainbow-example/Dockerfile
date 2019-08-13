FROM ubuntu:16.04 AS builder
#maven:3-jdk-8 AS builder
ARG rainbow_version=SWIM1.0
RUN apt update && apt-get install --no-install-recommends -y software-properties-common zip unzip tar gzip libboost-all-dev libyaml-cpp-dev make automake autoconf g++ ant wget libpcre3-dev socat swig curl bash git-core

# To ensure the right versions of the libraries in the build
# we need to FROM the same root, so that means installing Java
# and Maven manually

RUN apt-get install -y openjdk-8-jdk

# Install Maven
ARG MAVEN_VERSION=3.3.9
ARG USER_HOME_DIR="/root"
RUN mkdir -p /usr/share/maven && \
curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar -xzC /usr/share/maven --strip-components=1 && \
ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

ADD libs /root/rainbow/libs
WORKDIR /root/rainbow/libs/auxtestlib
RUN mvn -DskipTests install
WORKDIR /root/rainbow/libs/incubator
RUN mvn -DskipTests install
WORKDIR /root/rainbow/libs/parsec
RUN mvn -DskipTests javacc:javacc install
WORKDIR /root/rainbow/libs/typelib
RUN mvn -DskipTests javacc:javacc install
WORKDIR /root/rainbow/libs/eseblib
RUN mvn -DskipTests install

ADD rainbow /root/rainbow/rainbow
WORKDIR /root/rainbow/rainbow/rainbow-core
RUN mvn -DskipTests install
WORKDIR ../rainbow-gui
RUN mvn -DskipTests install
WORKDIR ../rainbow-acme-model
RUN mvn -DskipTests install
WORKDIR ../rainbow-stitch
RUN mvn -DskipTests install
WORKDIR ../rainbow-utility-model
RUN mvn -DskipTests install

WORKDIR ../rainbow-mem-comms
RUN mvn -DskipTests install

ADD deployments /root/rainbow/deployments
WORKDIR /root/rainbow/deployments/rainbow-example
RUN mvn -DskipTests install
CMD [/bin/bash]


FROM gabrielmoreno/swim:1.0
ARG BUILD_ENV
RUN apt update && DEBIAN_FRONTEND=noninteractive apt-get -y install software-properties-common dbus dbus-x11 xorg xserver-xorg-legacy xinit xterm libboost-all-dev libyaml-cpp-dev libpcre3-dev socat

RUN sed -i "s/allowed_users=console/allowed_users=anybody/;$ a needs_root_rights=yes" /etc/X11/Xwrapper.config

RUN \
  add-apt-repository -y ppa:openjdk-r/ppa && \
  apt-get update && \
  apt-get -y install openjdk-8-jdk

RUN mkdir /rainbow
WORKDIR /rainbow
COPY scripts/* ./
COPY targets/rainbow-example targets/rainbow-example
COPY license.html .
COPY --from=builder /root/rainbow/deployments/rainbow-example/target/lib/* lib/
COPY --from=builder /root/rainbow/deployments/rainbow-example/target/*.jar lib/


RUN echo "export SOCAT_PORT=4242" >> /headless/.bashrc
RUN echo "export JAVA_FONTS=/usr/local/share/fonts/ms_fonts" >> /headless/.bashrc
COPY deployments/rainbow-example/docker/rainbow.png /headless/
COPY deployments/rainbow-example/docker/SWIM.png /headless/
COPY deployments/rainbow-example/docker/Rainbow.desktop /headless/Desktop
#COPY deployments/rainbow-example/docker/SWIM.desktop /headless/Desktop
COPY deployments/rainbow-example/docker/Rainbow-debug.desktop /headless/Desktop
COPY deployments/rainbow-example/docker/SWIM-driver.desktop /headless/Desktop
COPY deployments/rainbow-example/docker/swim-driver.sh /rainbow
RUN chmod +x /rainbow/swim-driver.sh
RUN echo "BUILD_ENV=$BUILD_ENV"
RUN if [ "$BUILD_ENV" != "debug" ]; then rm -f /headless/Desktop/Rainbow-debug.desktop; fi
RUN chmod +x /headless/Desktop/*
RUN mkdir /rainbow/logs

# Add a test trace
WORKDIR /headless/seams-swim/swim/simulations/swim
COPY deployments/rainbow-example/docker/sudden-increase.delta traces
RUN sed 's/delta",/delta", "traces\/sudden-increase.delta",/' < swim.ini > swim-new.ini && \
    mv swim-new.ini swim.ini
#ENTRYPOINT ["./rainbow-oracle.sh", "swim"]
