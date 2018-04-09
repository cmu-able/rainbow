// ----------------------------------------------------------------------------
// turtlebot-ros.als - ROS turtlebot reconfiguration
//-----------------------------------------------------------------------------

// Components
abstract sig component {}

abstract sig sensing extends component {}
abstract sig localization extends component {}


lone sig kinect extends sensing {}
lone sig lidar extends sensing {}
lone sig camera extends sensing {} // Simplified front-and back to just one type (always go together)

lone sig amcl extends localization {}
lone sig mrpt extends localization {}
lone sig markerLocalization extends localization{}

lone sig laserscanNodelet extends component {}
lone sig markerRecognizer extends component {} // Simplified to just one type (like cameras)
lone sig headlamp extends component {}

// Options
abstract sig option {}

abstract sig speedSetting extends option {}

lone sig halfSpeedSetting extends speedSetting {}
lone sig fullSpeedSetting extends speedSetting {}

// Constraints
pred config{
  some kinect <=> some laserscanNodelet
  some camera <=> some markerLocalization
  some camera <=> some markerRecognizer
  some headlamp => some camera
  one sensing
  one localization
  one speedSetting
}

// Synthesis command
run config for 1
