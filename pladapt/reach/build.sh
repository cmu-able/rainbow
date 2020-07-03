#!/bin/sh
ALLOYJAR=alloy4.2.jar
ALLOYURL=http://alloytools.org/download/alloy4.2_2015-02-22.jar
YAMLJAR=yamlbeans-1.11.jar
YAMLURL=https://github.com/EsotericSoftware/yamlbeans/releases/download/1.11/yamlbeans-1.11.jar

BASEDIR=$(dirname "$0")
LIBDIR=${BASEDIR}/lib
mkdir -p ${LIBDIR}
wget -nc -O ${LIBDIR}/${ALLOYJAR} ${ALLOYURL}

wget -nc -O ${LIBDIR}/${YAMLJAR} ${YAMLURL}
ln -s ${YAMLJAR} ${LIBDIR}/yamlbeans.jar  

ant -buildfile ${BASEDIR}/build.xml
