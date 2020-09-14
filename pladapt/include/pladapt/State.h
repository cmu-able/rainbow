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
 * State.h
 *
 *  Created on: Aug 11, 2015
 *      Author: ashutosp
 */

#ifndef STATE_H_
#define STATE_H_

#include <string>
using namespace std;

class State {
public:
    unsigned m_s;
    unsigned m_time;
    bool m_readyToTick;
    unsigned m_active_servers_A;
    unsigned m_active_servers_B;
    unsigned m_active_servers_C;
    unsigned m_dimmer;
    unsigned m_traffic_A;
    unsigned m_traffic_B;
    unsigned m_traffic_C;
    bool m_removeServer_go;
    bool m_removeServer_used;
    unsigned m_addServerA_state;
    unsigned m_addServerB_state;
    unsigned m_addServerC_state;
    bool m_addServer_go;
    bool m_divert_go;
    bool m_increaseDimmer_go;
    bool m_increaseDimmer_used;
    bool m_decreaseDimmer_go;
    bool m_decreaseDimmer_used;

    State() :
            m_s(0), m_time(0), m_readyToTick(false), m_active_servers_A(0), m_active_servers_B(
                    0), m_active_servers_C(0), m_dimmer(0), m_traffic_A(
                    0), m_traffic_B(0), m_traffic_C(0), m_removeServer_go(
                    false), m_removeServer_used(false), m_addServerA_state(0), m_addServerB_state(
                    0), m_addServerC_state(0), m_addServer_go(false), m_divert_go(
                    false), m_increaseDimmer_go(false), m_increaseDimmer_used(
                    false), m_decreaseDimmer_go(false), m_decreaseDimmer_used(
                    false) {
    }

    State (unsigned s,
            unsigned time,
            bool readyToTick,
            unsigned active_servers_A,
            unsigned active_servers_B,
            unsigned active_servers_C,
            unsigned dimmer,
            unsigned traffic_A,
            unsigned traffic_B,
            unsigned traffic_C,
            bool removeServer_go,
            bool removeServer_used,
            unsigned addServerA_state,
            unsigned addServerB_state,
            unsigned addServerC_state,
            bool addServer_go,
            bool divert_go,
            bool increaseDimmer_go,
            bool increaseDimmer_used,
            bool decreaseDimmer_go,
            bool decreaseDimmer_used);

    virtual ~State();
    std::string get_state_str();
    std::string get_bool_str(bool var);
};

#endif /* STATE_H_ */
