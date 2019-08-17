SOURCES=$(shell find . -name "*.java")
CLASSPATH="../*"

.PHONY: classfiles test

classfiles: $(SOURCES)
	javac -classpath $(CLASSPATH) $^

test: classfiles
	LD_LIBRARY_PATH=.. java -classpath ".:../*" test
	LD_LIBRARY_PATH=.. java -classpath ".:../*" TestUtilityFunction
	LD_LIBRARY_PATH=.. java -classpath ".:../*" TestUtilityFunction2
	LD_LIBRARY_PATH=.. java -classpath ".:../*" TestTimeSeries
	LD_LIBRARY_PATH=.. java -classpath ".:../*" TestSDPAdaptationManager
clean:
	$(RM) *.class *~


