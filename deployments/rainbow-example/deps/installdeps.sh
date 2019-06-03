#!/bin/sh

CWD=`pwd`

cd `dirname $0`
echo "Installing Hogna (see http://www.ceraslabs.com/hogna)..."
unzip hogna.zip libs/Hogna.jar libs/Opera.jar
mvn install:install-file -Dfile=libs/Hogna.jar -DgroupId=opera -DartifactId=hogna -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/Opera.jar -DgroupId=opera -DartifactId=opera -Dversion=1.0 -Dpackaging=jar

# make sure we're removing the right directory
if [ -f libs/Hogna.jar ]; then
    rm -rf libs
fi

echo "done"

cd $CWD
