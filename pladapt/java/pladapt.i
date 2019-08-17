%module(directors="1") pladapt
%{
#include "pladapt/AdaptationPlanner.h"
#include "pladapt/timeseries/EnvPredictionRecord.h"
#include "pladapt/PlanDB.h"
#include "pladapt/State.h"
#include "pladapt/GenericConfiguration.h"
#include "pladapt/GenericEnvironment.h"
#include "pladapt/JointEnvironment.h"
#include "pladapt/GenericUtilityFunction.h"
#include "pladapt/GenericConfigurationManager.h"
#include "pladapt/EnvironmentDTMCPartitioned.h"
#include "pladapt/JavaSDPAdaptationManager.h"
#include "pladapt/GenericEnvironmentDTMCPartitioned.h"
#include "pladapt/timeseries/ScenarioTree.h"
#include "pladapt/timeseries/TimeSeriesPredictor.h"
#include "pladapt/PMCAdaptationManager.h"
#include <string>
#include <vector>
#include <set>
#include <sstream>
#include <math.h>
#include <float.h>
  namespace pladapt {
    double testGeneric(const GenericUtilityFunction& u, const GenericConfiguration& c, const GenericEnvironment& e);
    double testUtilityFunction(const UtilityFunction& u, const Configuration& c, const Environment& e);
    double testUtilityFunctionWithConfigMgr(const UtilityFunction& u, const GenericConfigurationManager& cm, const Environment& e);
  }
%}

%include "std_string.i"

//%javaexception("java.lang.Exception") {
%exception {
  try {
     $action
  } catch (std::exception &e) {
    jclass clazz = jenv->FindClass("java/lang/Exception");
    jenv->ThrowNew(clazz, e.what());
    return $null;
   }
}

%include "std_string.i"
%include "pladapt/AdaptationPlanner.h"
//%include "pladapt/timeseries/EnvPredictionRecord.h"
%include "pladapt/PlanDB.h"
//%include "pladapt/State.h"

%include <std_shared_ptr.i>
%include "std_string.i"

// deal with operators
%ignore operator<<;
%ignore operator==;


// for the Class enums in Environment and Configuration
%include "enumtypeunsafe.swg"
%javaconst(1);

 //%shared_ptr(pladapt::Environment)
%feature("director") pladapt::Environment;
// from https://stackoverflow.com/questions/42349170/passing-java-object-to-c-using-swig-then-back-to-java/42378259#42378259
// Call our extra Java code to figure out if this was really a Java object to begin with
%typemap(javadirectorin) const pladapt::Environment & "$jniinput == 0 ? null : new $*javaclassname($jniinput, false).swigFindRealImpl()"
// Pass jenv into our %extend code
%typemap(in,numinputs=0) JNIEnv *jenv "$1 = jenv;"
%extend pladapt::Environment {
    // return the underlying Java object if this is a Director, or null otherwise
    jobject swigOriginalObject(JNIEnv *jenv) {
        Swig::Director *dir = dynamic_cast<Swig::Director*>($self);
        //std::cerr << "Dynamic_cast: " << dir << "\n";
        if (dir) {
            return dir->swig_get_self(jenv);
        }
        return NULL;
    }
}
%typemap(javacode) pladapt::Environment %{
  public Environment swigFindRealImpl() {
     Object o = swigOriginalObject();
     if (o != null) {
     	return ($javaclassname)o;
     } else {
	    Environment ret = ($javaclassname) this; // default if it's the base class
	    int type = $imclassname.Environment_getType(swigCPtr, null);
	    switch (type) {
	       case Environment.EnvironmentClass.C_ENVIRONMENT:
	    	 break;
	       case Environment.EnvironmentClass.C_GENERIC_ENVIRONMENT:
	         ret = new GenericEnvironment(swigCPtr, swigCMemOwn);
	         break;
	       case Environment.EnvironmentClass.C_JOINT_ENVIRONMENT:
	         ret = new JointEnvironment(swigCPtr, swigCMemOwn);
	         break;
	      default:
	        System.out.println("Instance of unknown Environment class found");
	        break;
		}
      	return ret;
     }
  }
%}
%ignore pladapt::Environment::printOn;
%include "pladapt/Environment.h"

%ignore pladapt::GenericProperties;
%include "pladapt/GenericProperties.h"


%feature("director") pladapt::Configuration;
// based on https://stackoverflow.com/questions/42349170/passing-java-object-to-c-using-swig-then-back-to-java/42378259#42378259
// Call our extra Java code to figure out if this was really a Java object to begin with
%typemap(javadirectorin) const pladapt::Configuration & "$jniinput == 0 ? null : new $*javaclassname($jniinput, false).swigFindRealImpl()"
// Pass jenv into our %extend code
%typemap(in,numinputs=0) JNIEnv *jenv "$1 = jenv;"
%extend pladapt::Configuration {
    // return the underlying Java object if this is a Director, or null otherwise
    jobject swigOriginalObject(JNIEnv *jenv) {
        Swig::Director *dir = dynamic_cast<Swig::Director*>($self);
        //std::cerr << "Dynamic_cast: " << dir << "\n";
        if (dir) {
            return dir->swig_get_self(jenv);
        }
        return NULL;
    }
}
%typemap(javacode) pladapt::Configuration %{
  public Configuration swigFindRealImpl() {
     Object o = swigOriginalObject();
     if (o != null) {
     	return ($javaclassname)o;
     } else {
	    Configuration ret = ($javaclassname) this; // default if it's the base class
	    int type = $imclassname.Configuration_getType(swigCPtr, null);
	    switch (type) {
	       case Configuration.ConfigurationClass.C_CONFIGURATION:
	    	 break;
	       case Configuration.ConfigurationClass.C_GENERIC_CONFIGURATION:
	         ret = new GenericConfiguration(swigCPtr, swigCMemOwn);
	         break;
	      default:
	        System.out.println("Instance of unknown Configuration class found");
	        break;
		}
      	return ret;
     }
  }
%}

%ignore pladapt::Configuration::printOn;
%include "pladapt/Configuration.h"


%feature("director") pladapt::GenericConfiguration;
%ignore pladapt::GenericConfiguration::printOn;
%include "pladapt/GenericConfiguration.h"
%template(getDouble) pladapt::GenericConfiguration::get<double>;
%template(setDouble) pladapt::GenericConfiguration::set<double>;
%template(getInt) pladapt::GenericConfiguration::get<int>;
%template(setInt) pladapt::GenericConfiguration::set<int>;
%template(getBool) pladapt::GenericConfiguration::get<bool>;
%template(setBool) pladapt::GenericConfiguration::set<bool>;

//%shared_ptr(pladapt::GenericEnvironment)
%feature("director") pladapt::GenericEnvironment;
%ignore pladapt::GenericEnvironment::printOn;
%include "pladapt/GenericEnvironment.h"
%template(getDouble) pladapt::GenericEnvironment::get<double>;
%template(setDouble) pladapt::GenericEnvironment::set<double>;
%template(getInt) pladapt::GenericEnvironment::get<int>;
%template(setInt) pladapt::GenericEnvironment::set<int>;

%feature("director") pladapt::JointEnvironment;
%ignore pladapt::JointEnvironment::printOn;
%include "pladapt/JointEnvironment.h"

%shared_ptr(pladapt::GenericConfigurationManager)
%ignore pladapt::GenericConfigurationManager::getConfigurationFromYaml;
%ignore pladapt::GenericConfigurationManager::getConfigurationSpace;
%include "pladapt/GenericConfigurationManager.h"

%feature("director") pladapt::UtilityFunction;
%ignore pladapt::UtilityFunction::printOn;
%include "pladapt/UtilityFunction.h"

%feature("director") pladapt::GenericUtilityFunction;
%feature("nodirector") pladapt::GenericUtilityFunction::getAdditiveUtility;
%feature("nodirector") pladapt::GenericUtilityFunction::getMultiplicativeUtility;
%feature("nodirector") pladapt::GenericUtilityFunction::getFinalReward;
%feature("nodirector") pladapt::GenericUtilityFunction::getAdaptationReward;
%ignore pladapt::GenericUtilityFunction::getAdditiveUtility;
%ignore pladapt::GenericUtilityFunction::getMultiplicativeUtility;
%ignore pladapt::GenericUtilityFunction::getFinalReward;
%ignore pladapt::GenericUtilityFunction::getAdaptationReward;
%include "pladapt/GenericUtilityFunction.h"

 // these methods should not be needed outside of the evaluation
%ignore pladapt::DTMCPartitionedStates::getNumberOfStates;
%ignore pladapt::DTMCPartitionedStates::getTransitionMatrix;
%ignore pladapt::DTMCPartitionedStates::getPart;
%include "pladapt/DTMCPartitionedStates.h"


%ignore pladapt::EnvironmentDTMCPartitioned::printOn;
%ignore pladapt::EnvironmentDTMCPartitioned::getStateValue;
%ignore pladapt::EnvironmentDTMCPartitioned::setStateValue(unsigned state, std::shared_ptr<Environment> pValue);
%include "pladapt/EnvironmentDTMCPartitioned.h"


%include "pladapt/GenericEnvironmentDTMCPartitioned.h"


// add toString methods
namespace pladapt {
  %extend Configuration {
    std::string toString() {
      std::ostringstream os;
      $self->printOn(os);
      return os.str();
    }
   }
  %extend Environment {
    std::string toString() {
      std::ostringstream os;
      $self->printOn(os);
      return os.str();
    }
   }
  %extend GenericConfiguration {
    std::string toString() {
      std::ostringstream os;
      $self->printOn(os);
      return os.str();
    }
   }
  %extend GenericEnvironment {
    std::string toString() {
      std::ostringstream os;
      $self->printOn(os);
      return os.str();
    }
   }
  %extend EnvironmentDTMCPartitioned {
    std::string toString() {
      std::ostringstream os;
      $self->printOn(os);
      return os.str();
    }
   }
}



%include <std_vector.i>
%template(StringVector) std::vector<std::string>;
%ignore pladapt::AdaptationManager::evaluate(const Configuration& currentConfigObj, const EnvironmentDTMCPartitioned& envDTMC,
    		const UtilityFunction& utilityFunction, unsigned horizon);
%include "pladapt/AdaptationManager.h"

%shared_ptr(pladapt::ConfigurationManager)
%ignore pladapt::SDPAdaptationManager::initialize(std::shared_ptr<const ConfigurationManager> configMgr, const YAML::Node& params);
%ignore pladapt::SDPAdaptationManager::evaluate(const Configuration& currentConfigObj, const EnvironmentDTMCPartitioned& envDTMC,
   const UtilityFunction& utilityFunction, unsigned horizon);
%immutable;
%include "pladapt/SDPAdaptationManager.h"
%mutable;

%rename(evaluate) evaluateWrapper; 
%include "pladapt/JavaSDPAdaptationManager.h"

namespace pladapt {
  double testGeneric(const GenericUtilityFunction& u, const GenericConfiguration& c, const GenericEnvironment& e);
  double testUtilityFunction(const UtilityFunction& u, const Configuration& c, const Environment& e);  
  double testUtilityFunctionWithConfigMgr(const UtilityFunction& u, const GenericConfigurationManager& cm, const Environment& e);
}

%include "arrays_java.i"
%apply double[] {double *};
%ignore pladapt::timeseries::TimeSeriesPredictor::createScenarioTree;
%include "pladapt/timeseries/TimeSeriesPredictor.h"

namespace pladapt {
  namespace timeseries {
  %extend TimeSeriesPredictor {
    EnvironmentDTMCPartitioned generateEnvironmentDTMC(unsigned branchingDepth, unsigned depth = 0, double lowerBound = -DBL_MAX) {
  auto pTree = $self->createScenarioTree(lowerBound, DBL_MAX, branchingDepth, depth);
  auto envDTMC = pTree->getEnvironmentDTMC(
					   [](double mean) {auto envState = std::make_shared<pladapt::GenericEnvironment>("m"); envState->set<double>("m", mean); envState->set<double>("v", pow(mean, 2)); return envState;}); // assume exponential
      delete pTree;
      return envDTMC;
    }
   }
 }
}

%ignore pladapt::PMCHelper;
%ignore pladapt::PMCAdaptationManager::initialize;
%ignore pladapt::PMCAdaptationManager::evaluate;
%include "pladapt/PMCAdaptationManager.h"
