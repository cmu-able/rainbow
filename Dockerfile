FROM maven:3-jdk-8
#----
# Install Maven
RUN apt-get install -y tar gzip
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
ENTRYPOINT ["/root/rainbow/build.sh", "-d", "/root/rainbow/"]

