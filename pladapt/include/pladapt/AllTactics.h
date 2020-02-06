//
 // Copyright (c) 2015 Carnegie Mellon University. All Rights Reserved.

 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions
 // are met:

 // 1. Redistributions of source code must retain the above copyright
 // notice, this list of conditions and the following acknowledgments
 // and disclaimers.

 // 2. Redistributions in binary form must reproduce the above
 // copyright notice, this list of conditions and the following
 // disclaimer in the documentation and/or other materials provided
 // with the distribution.

 // 3. The names "Carnegie Mellon University," "SEI" and/or "Software
 // Engineering Institute" shall not be used to endorse or promote
 // products derived from this software without prior written
 // permission. For written permission, please contact
 // permission@sei.cmu.edu.

 // 4. Products derived from this software may not be called "SEI" nor
 // may "SEI" appear in their names without prior written permission of
 // permission@sei.cmu.edu.

 // 5. Redistributions of any form whatsoever must retain the following
 // acknowledgment:

 // This material is based upon work funded and supported by the
 // Department of Defense under Contract No. FA8721-05-C-0003 with
 // Carnegie Mellon University for the operation of the Software
 // Engineering Institute, a federally funded research and development
 // center.

 // Any opinions, findings and conclusions or recommendations expressed
 // in this material are those of the author(s) and do not necessarily
 // reflect the views of the United States Department of Defense.

 // NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE
 // ENGINEERING INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS"
 // BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY KIND,
 // EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER INCLUDING, BUT NOT
 // LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE OR MERCHANTABILITY,
 // EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE MATERIAL. CARNEGIE
 // MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF ANY KIND WITH
 // RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT
 // INFRINGEMENT.

 // This material has been approved for public release and unlimited
 // distribution.

 // DM-0002494
 //
/*
 * AllTactics.h
 *
 *  Created on: Jan 3, 2015
 *      Author: gmoreno
 */

#ifndef ALLTACTICS_H_
#define ALLTACTICS_H_

//#include "MacroTactic.h"
//#include "AddServerTactic.h"
//#include "RemoveServerTactic.h"
//#include "SetBrownoutTactic.h"
//#include "HPAddServerTactic.h"
//#include "HPRemoveServerTactic.h"
//#include "HPDivertTraffic.h"

enum TacticEnum {
    NONE,
    TICK,
    ADD_SERVER_A_START,
    ADD_SERVER_B_START,
    ADD_SERVER_C_START,
    REMOVE_SERVER_A_START,
    REMOVE_SERVER_B_START,
    REMOVE_SERVER_C_START,
    INC_DIMMER,
    DEC_DIMMER,
    DIVERT_TRAFFIC_100_0_0,
    DIVERT_TRAFFIC_75_25_0,
    DIVERT_TRAFFIC_75_0_25,
    DIVERT_TRAFFIC_50_50_0,
    DIVERT_TRAFFIC_50_0_50,
    DIVERT_TRAFFIC_50_25_25,
    DIVERT_TRAFFIC_25_75_0,
    DIVERT_TRAFFIC_25_0_75,
    DIVERT_TRAFFIC_25_50_25,
    DIVERT_TRAFFIC_25_25_50,
    DIVERT_TRAFFIC_0_100_0,
    DIVERT_TRAFFIC_0_0_100,
    DIVERT_TRAFFIC_0_75_25,
    DIVERT_TRAFFIC_0_25_75,
    DIVERT_TRAFFIC_0_50_50,
    PROGRESS_A,
    PROGRESS_B,
    PROGRESS_C,
    ADD_SERVER_A_COMPLETE,
    ADD_SERVER_B_COMPLETE,
    ADD_SERVER_C_COMPLETE

};

#endif /* ALLTACTICS_H_ */
