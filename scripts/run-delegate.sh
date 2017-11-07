#!/bin/bash
TARGET=znews-ss
DEBUG=""

while getopts :d opt; do
  case $opt in
    d)
	  DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044"
	  ;;
   esac
done
shift $((OPTIND-1))

if [[ "$#" == 1 ]]; then
  TARGET=$1
else
  echo "Warning: target not passed as a parameter, using $TARGET as default"
fi

# Check if rainbow.properties exists.
TARGET_DIR="targets/$TARGET"
if [ ! -d "$TARGET_DIR" ]; then
	echo "Error: target directory $TARGET_DIR" does not exist.
	exit 1
fi

RB_PROPS="$TARGET_DIR/rainbow.properties"
if [ ! -f "$RB_PROPS" ]; then
	echo "Note: rainbow.properties file not found in $RB_PROPS."
	echo "Note: searching for one in $TARGET_DIR"
	FOUND=""
	for F in `cd "$TARGET_DIR" && ls`; do
		if [[ (-f "$TARGET_DIR/$F") && \
				("$F" =~ ^rainbow-(.+)\.properties$) ]]; then
			ADDRESS=${BASH_REMATCH[1]}
			COUNT=$(/sbin/ifconfig | egrep "inet addr:$ADDRESS" | wc -l)
			if [ "$COUNT" == "1" -a -n "$FOUND" ]; then
				echo "Multiple candidates found for rainbow.properties."
				exit 1
			elif [ "$COUNT" == "1" ]; then
				FOUND="$F"
			fi
		fi
	done

	if [ -z "$FOUND" ]; then
		echo "Error: rainbow.properties file not found ($RB_PROPS)"
		exit 1
	fi

	echo "Note: using $FOUND as rainbow.properties."
	(cd "$TARGET_DIR" && ln -s "$FOUND" rainbow.properties)
fi

java -classpath ".:lib/*"  -XX:+HeapDumpOnOutOfMemoryError $DEBUG -Drainbow.target=$TARGET org.sa.rainbow.core.RainbowDelegate
