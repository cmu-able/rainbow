# src
lib_LTLIBRARIES = libdartam.la

ADAPTMGR_PATH = $(top_srcdir)/../../..

AM_CPPFLAGS = -std=c++0x -I$(top_srcdir)/include -I$(ADAPTMGR_PATH)/include -O3 -Wall
ARFLAGS = cr

libdartam_la_SOURCES = DartAdaptationManager.cpp DartConfiguration.cpp \
	DartConfigurationManager.cpp DartDTMCEnvironment.cpp DartEnvironment.cpp \
	DartPMCHelper.cpp DartSimpleEnvironment.cpp DartUtilityFunction.cpp \
	DeterministicTargetSensor.cpp DeterministicThreat.cpp \
	EnvironmentMonitor.cpp RandomSeed.cpp RealEnvironment.cpp Route.cpp \
	Sensor.cpp TargetSensor.cpp Threat.cpp
libdartam_la_LIBADD = $(ADAPTMGR_PATH)/build/src/libadaptmgr.a -lboost_system \
	-lboost_filesystem -lboost_serialization -lyaml-cpp
