#!/bin/bash
PROG=$0
target=install
jcctarget="javacc:javacc install"
TIMESTAMP=`date +'%Y%m%d%H%M'`
VERSION=$TIMESTAMP


function usage () {
  echo "Usage $PROG [-d deployment-dir] [-t target] [-v version] [command]"
  echo "    -d -the deployment that you want included in the build, either relative or in the dir deployments"
  echo "    -t -the comma separated list of targets to include in the build, either relative or in the targets dir"
  echo "    -v -the version label to give the release"
  echo "command -the build command to give"
}

while getopts :d:t:v: opt; do
  case $opt in
    d)
      if [ -d "$OPTARG" ]; then
        DEPLOYMENT=$OPTARG
      elif [ -d "deployments/$OPTARG" ]; then
        DEPLOYMENT="deployments/$OPTARG" 
      else
        echo "'$OPTARG' not found either relative or in deployments directory"
        usage
        exit 1
      fi
      ;;
    t)
      IFS=, read -r -a targets <<< $OPTARG
      TARGETS=()
      for i in "${targets[@]}"; do
        if [ -d "$i" ]; then
          TARGETS+=($i)
        elif [ -d "targets/$i" ]; then
          TARGETS+=("targets/$i")
        else
          echo "'$i' was not found as a target (even in targets/)"
        fi
      done
      if [ ${#TARGETS[@]} -eq 0 ]; then
        echo "None of the targets specified as an argument to -t exist; presuming error!"
        usage
        exit 1
      fi
      ;;
    v)
      VERSION=$OPTARG"-$TIMESTAMP"
      ;;
    \?)
      usage
      exit 1
      ;;
    :)
      usage
      exit 1
      ;;
  esac
done
shift $((OPTIND-1))

if [ ! -d "rainbow" -o ! -d "libs" ]; then
  echo "FATAL: Could not find the rainbow directory containing sourc for the core Rainbow components and libraries!"
  exit 1
fi


if [[ "$#" == 1 ]]; then
  if [[ "$1" != "install" ]]; then
    target=$1
    jcctarget=$1
  fi
fi
echo "Doing $target"
mkdir -p bin/lib

cd libs/

cd auxtestlib
mvn $target
cd ../incubator
mvn $target
cd ../parsec
mvn $jcctarget -DskipTests
cd ../typelib
mvn $jcctarget
cd ../eseblib
mvn $target -DskipTests

cd ../../rainbow

cd rainbow-core
mvn $target -DskipTests
cd ../rainbow-acme-model
mvn $target -DskipTests
cd ../rainbow-utility-model
mvn $target 
cd ../rainbow-stitch
mvn $target

cd ../..
BUILDDIR=`pwd`
cd $DEPLOYMENT
mvn $target

if [[ "$target" == "install" ]]; then
  cp target/*.jar $BUILDDIR/bin/lib
  cp target/lib/* $BUILDDIR/bin/lib

  cd $BUILDDIR
  mkdir -p bin/targets

  for i in $TARGETS; do
    cp -r $i bin/targets
  done

  cp scripts/* bin/
  cp license.html bin/

  mv bin Rainbow-$VERSION
  zip Rainbow-$VERSION
elif [[ "$target" == "clean" ]]; then
  cd $BUILDDIR
  rm -rf Rainbow-$VERSION
  rm Rainbow-$VERSION.zip
fi






  