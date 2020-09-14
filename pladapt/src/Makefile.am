# src
# g++ -std=c++0x -I../include -O3 -g3 -Wall -c -fmessage-length=0 -fPIC
# ar -r
AUTOMAKE_OPTIONS = subdir-objects
lib_LIBRARIES = libadaptmgr.a

AM_CPPFLAGS = -std=c++0x -I$(top_srcdir)/include -O3 -Wall -fPIC
ARFLAGS = cr

libadaptmgr_a_SOURCES = AdaptationManager.cpp Configuration.cpp \
	AdaptationPlanner.cpp timeseries/EnvPredictionRecord.cc \
	State.cc PlanDB.cc \
	ConfigurationManager.cpp ConfigurationSpace.cpp DTMCPartitionedStates.cpp \
	Environment.cpp EnvironmentDTMCPartitioned.cpp GenericConfiguration.cpp \
	GenericConfigurationManager.cpp GenericEnvironment.cpp \
	GenericEnvironmentDTMCPartitioned.cpp GenericProperties.cpp \
	GenericUtilityFunction.cpp JavaSDPAdaptationManager.cpp \
	JointEnvironment.cpp PMCAdaptationManager.cpp PMCRAAdaptationManager.cpp \
	PRISMWrapper.cpp ReachabilityRelation.cpp SDPAdaptationManager.cpp \
	SDPRAAdaptationManager.cpp \
	timeseries/LESTimeSeriesPredictor.cpp \
	timeseries/OversamplingTimeSeriesPredictor.cpp timeseries/ScenarioTree.cpp \
	timeseries/TimeSeriesPredictor.cpp \
	UtilityFunction.cpp Utils.cpp WrapperTest.cpp

