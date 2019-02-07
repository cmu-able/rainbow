FROM openjdk:8-jdk
#----
# Install Maven
RUN apt-get install -y curl tar gzip
ARG MAVEN_VERSION=3.3.9
ARG USER_HOME_DIR=/root
RUN mkdir -p /usr/share/maven 
RUN curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar -xzC /usr/share/maven --strip-components=1 
RUN    ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
#ENTRYPOINT ["/bin/sh"]
ENTRYPOINT ["/root/rainbow/build.sh", "-d", "/root/rainbow/"]
#CMD [/bin/sh]

