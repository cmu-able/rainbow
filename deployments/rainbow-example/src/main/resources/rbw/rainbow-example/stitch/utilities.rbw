target rainbow-example
import properties "../properties.rbw"
import stitch "swimTactics.t.s"
def utility utilityModel = {
	^model = ««SwimSys»»
	utilities = {
		uR = {
			label="Average Response Time"
			mapping="[EXPR]LB0.optResponseTime"
			description="Client experienced response time in milliseconds, R, defined as a float property 'ClientT.experRespTime' in the architecture"
			^utility=[
				[0.0,1],
				[0.1,1],
				[0.2,0.99],
				[0.5,0.9],
				[1.0,0.75],
				[1.5,0.5],
				[2.0,0.25],
				[4.0,0]
			]
		}
		uC = {
			label= "Average Server Cost"
    		mapping= "[EXPR]size(/self/components:ServerT[isArchEnabled])"
    		description= "Server cost in unit/hr, C, averaged from a float property 'ServerT.cost' in the architecture, and captures average cost across all servers. Adapted to a 3-server configuration."
    		^utility=[
    			[0, 1.00],
      			[1, 0.90],
      			[2, 0.30],
      			[3, 0.10]
    		]
      
		}
		uA = {
    		label= "Average User Annoyance"
    		mapping= "[CALL]org.sa.rainbow.stitch.lib.SwimUtils.dimmerFactorToLevel(LB0.dimmer,DIMMER_LEVELS,DIMMER_MARGIN)"
    		description= "Client experienced annoyance level, should be defined as an int property 'ClientT.annoyance' in the architecture, with a range 0-100"
    		^utility = [
      			[0, 0],
      			[5, 1]
			]
		}
	}
	scenarios = [{
			name="scenario 1"
			uR=0.1
			uC=0.2
			uA=0.7
		}
	]
}


impact  ««utilityModel»» TAddServer = {  
	uR = -1.00
	uC = 1.00
	uA = 0  
}

impact  ««utilityModel»» TRemoveServer = {
	uR= 1
	uC= -1
	uA= 0  
} 

impact  ««utilityModel»» TIncDimmer = { 
	uR= -0.5
	uC= -0.1
	uA= 5
}

impact ««utilityModel»» TDecDimmer = {
	uR= 5
	uC= 0.1
	uA= -5
}

export * to "stitch/utilities.yml"