#!/bin/bash
function usage() {
  echo "$0 [-d] (-r <propfile> | -m <masterip>) [-p port] target"
}

function genPropFile() {
  file="$1"
  master="$2"
  port="$3"
  
  echo "rainbow.master.location.host = $master" > $1
  echo "rainbow.master.location.port = $port" >> $1
  echo "rainbow.deployment.environment = linux" >> $1
  echo "rainbow.deployment.factory.class = org.sa.rainbow.core.ports.eseb.ESEBRainbowPortFactory" >> $1
}

TARGET=znews-ss
DEBUG=""
PROP=""
master=""
port="1100"
HEADLESS=""

while getopts :dhr:m:p: opt; do
  case $opt in
    d)
	  DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044"
	  ;;
	r)
	  PROP="$OPTARG"
	  ;;
	m)
	  master="$OPTARG"
	  ;;
	p)
	  port="$OPTARG"
	  ;;
	h)
	  HEADLESS="y"
	  ;;
  esac
done
shift $((OPTIND-1))

if [[ "$#" == 1 ]]; then
  TARGET=$1
else
  echo "Warniing: target not passed as a parameter; using $TARGET as target"
fi


#Check if rainbow properties file exists
TARGET_DIR="targets/$TARGET"
if [ ! -d "$TARGET_DIR" ]; then
  echo "Error: target directory ''$TARGET_DIR'' does not exist"
  exit 1
fi

echo $DEBUG $PROP $master $port $TARGET


#Check to see if we should generate a properties file
if [ "$master" == "" ]; then
  if [ "$PROP" == "" ]; then
    echo "Error: -r or -m params required"
	usage 
	exit 1;
  fi
else 
  if [ "$PROP" == "" ]; then
    PROP="rainbow-gui-generated.properties"
	DELETE="true"
  else
    if [ -e "$TARGET_DIR/$PROP" ]; then
	  echo -n "$TARGET_DIR/$PROP exists. Overwrite? [y/N]"
	  read answer
	  if [ "$answer" != "y" ]; then
	    $master=""
	  fi
	fi
  fi
  if [ "$master" != "" ]; then
    genPropFile "$TARGET_DIR/$PROP" "$master" "$port"
  fi
fi

if [ "$(uname)" == "Darwin" ]; then
  delim=":"
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
  delim=":"
elif [ "$(expr substr $(uname -o) 1 6)" == "Cygwin" ]; then
  delim=";"
fi

if [ "$HEADLESS" != "y" ]; then
  echo "java -classpath .${delim}lib/* -Drainbow.target=$TARGET -Drainbow.propfile=$PROP $DEBUG org.sa.rainbow.gui.RainbowGUI"
  java -classpath ".${delim}lib/*" -Drainbow.target=$TARGET -Drainbow.propfile=$PROP $DEBUG org.sa.rainbow.gui.RainbowGUI
else
  echo "java -classpath .${delim}lib/* -Drainbow.target=$TARGET -Drainbow.propfile=$PROP $DEBUG org.sa.rainbow.gui.CommandLineUI"
  java -classpath ".${delim}lib/*" -Drainbow.target=$TARGET -Drainbow.propfile=$PROP $DEBUG org.sa.rainbow.gui.CommandLineUI
fi

if [ "$DELETE" == "true" ]; then
  rm -f "$TARGET_DIR/$PROP"
fi
