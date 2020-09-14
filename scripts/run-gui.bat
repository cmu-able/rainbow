SET target=znews-ss
SET debug=""

:parse
if "%~1"=="" (
  goto endparse
)
else if "%~1"=="-d" (
  set debug="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044"
)
else (
  set target="%~1"
)
SHIFT
goto parse
:endparse

java -classpath=".;lib/*" -XX:+HeapDumpOnOutOfMemoryError -Drainbow.target=%target% org.sa.rainbow.gui.RainbowGUI