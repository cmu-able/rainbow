BASENAME = pladapt
VERSION = 1.0.1
ARCH = $(shell uname -i)
WRAPPERNAME = $(BASENAME)_wrap
JAVATARGETDIR = src/$(BASENAME)
TARGETLIB = lib$(WRAPPERNAME).so
TARGETJAR = $(WRAPPERNAME).jar
OBJS = $(WRAPPERNAME).o
JDK_HOME = $(shell dirname $$(readlink -f `which javac`))/..
INCLUDEDIRS = -I$(JDK_HOME)/include -I$(JDK_HOME)/include/linux -I../include
CXXFLAGS = -g -std=c++11 -fPIC -Wall -fno-strict-aliasing $(INCLUDEDIRS) -Wno-unused-function
LDLIBS = -ladaptmgr -lyaml-cpp -lboost_system -lboost_filesystem
LDFLAGS = -g -L../build/src/

all: $(TARGETLIB) $(TARGETJAR)


$(TARGETLIB): $(OBJS)
	$(CXX) -shared $(OBJS) $(LDFLAGS) $(LDLIBS) -o $@

$(WRAPPERNAME).cpp: $(BASENAME).i $(JAVATARGETDIR)
	/home/frank/swig-3.0.12/swig -c++ -cppext cpp -java -package $(BASENAME) -outdir src/$(BASENAME) $(INCLUDEDIRS) $<

.PHONY: classfiles

classfiles: $(BASENAME).i $(JAVATARGETDIR) $(TARGETLIB)
	javac $(shell find src -name "*.java")

$(TARGETJAR): classfiles
	jar cf $@ $(shell cd src; find . -name "*.class" -printf '-C src %p\n')

$(JAVATARGETDIR):
	mkdir -p $@

clean:
	$(RM) $(OBJS) *.java *~ $(TARGETLIB) $(TARGETJAR) $(WRAPPERNAME).*
	$(RM) -r src
	$(MAKE) -C test clean

mvn-install: $(TARGETLIB) $(TARGETJAR)
	mvn install:install-file -Dfile=$(TARGETJAR) -DgroupId=$(BASENAME) -DartifactId=$(WRAPPERNAME) -Dversion=$(VERSION) -Dpackaging=jar
	mvn install:install-file -Dfile=$(TARGETLIB) -DgroupId=$(BASENAME) -DartifactId=lib$(WRAPPERNAME) -Dversion=$(VERSION) -Dpackaging=so -Dclassifier=$(ARCH)
