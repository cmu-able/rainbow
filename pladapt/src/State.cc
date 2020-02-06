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
 * State.cpp
 *
 *  Created on: Aug 11, 2015
 *      Author: ashutosp
 */

#include <pladapt/State.h>
#include <iostream>

State::State(unsigned s, unsigned time, bool readyToTick,
        unsigned active_servers_A, unsigned active_servers_B, unsigned active_servers_C,
        unsigned dimmer, unsigned traffic_A, unsigned traffic_B,
        unsigned traffic_C, bool removeServer_go, bool removeServer_used,
        unsigned addServerA_state, unsigned addServerB_state,
        unsigned addServerC_state, bool addServer_go, bool divert_go,
        bool increaseDimmer_go, bool increaseDimmer_used,
        bool decreaseDimmer_go, bool decreaseDimmer_used) :
    // TODO Auto-generated constructor stub
    m_s (s),
    m_time (time),
    m_readyToTick (readyToTick),
    m_active_servers_A (active_servers_A),
    m_active_servers_B (active_servers_B),
    m_active_servers_C (active_servers_C),
    m_dimmer (dimmer),
    m_traffic_A (traffic_A),
    m_traffic_B (traffic_B),
    m_traffic_C (traffic_C),
    m_removeServer_go (removeServer_go),
    m_removeServer_used (removeServer_used),
    m_addServerA_state (addServerA_state),
    m_addServerB_state (addServerB_state),
    m_addServerC_state (addServerC_state),
    m_addServer_go (addServer_go),
    m_divert_go (divert_go),
    m_increaseDimmer_go (increaseDimmer_go),
    m_increaseDimmer_used (increaseDimmer_used),
    m_decreaseDimmer_go (decreaseDimmer_go),
    m_decreaseDimmer_used (decreaseDimmer_used) {
}

State::~State() {
    // TODO Auto-generated destructor stub
}

string State::get_bool_str(bool var) {
    return var ? "true" : "false";
}

string State::get_state_str() {
    string state_str = "(";

    state_str += to_string(m_s) + ",";
    state_str += to_string(m_time) + ",";
    state_str += get_bool_str(m_readyToTick) + ",";
    state_str += to_string(m_active_servers_A) + ",";
    state_str += to_string(m_active_servers_B) + ",";
    state_str += to_string(m_active_servers_C) + ",";
    state_str += to_string(m_dimmer) + ",";
    state_str += to_string(m_traffic_A) + ",";
    state_str += to_string(m_traffic_B) + ",";
    state_str += to_string(m_traffic_C) + ",";
    state_str += get_bool_str(m_removeServer_go) + ",";
    state_str += get_bool_str(m_removeServer_used) + ",";
    state_str += to_string(m_addServerA_state) + ",";
    state_str += to_string(m_addServerB_state) + ",";
    state_str += to_string(m_addServerC_state) + ",";
    state_str += get_bool_str(m_addServer_go) + ",";
    state_str += get_bool_str(m_divert_go) + ",";
    state_str += get_bool_str(m_increaseDimmer_go) + ",";
    state_str += get_bool_str(m_increaseDimmer_used) + ",";
    state_str += get_bool_str(m_decreaseDimmer_go) + ",";
    state_str += get_bool_str(m_decreaseDimmer_used);
    state_str += ")";

    //cout << "State::get_state_str = " << state_str << endl;

    return state_str;
}
