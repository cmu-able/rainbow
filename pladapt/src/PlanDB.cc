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
 * PlanDB.cpp
 *
 *  Created on: Aug 5, 2015
 *      Author: ashutosp
 */

#include <pladapt/PlanDB.h>
#include <boost/tokenizer.hpp>
#include <fstream>
#include <sstream>
#include <iostream>
//#include <modules/MTServerAdvance.h>
#include "pladapt/timeseries/EnvPredictionRecord.h"

//namespace pladapt{

PlanDB* PlanDB::m_db_object = NULL;

bool debug = false;

std::vector<std::string> actions;

PlanDB::PlanDB() : m_states_file("result.sta"), m_adversary_file("result.adv") {
    // TODO Auto-generated constructor stub
}

PlanDB::~PlanDB() {
    // TODO Auto-generated destructor stub
}

void PlanDB::update_val
    (int horizon,
    int current_time,
    int active_server_A,
    int active_server_B,
    int active_server_C,
    int dimmer_level,
    int traffic_A,
    int traffic_B,
    int traffic_C,
    int boot_remain,
    double boot_delay,
    double evaluation_period,
    int boot_type,
    double arrival_mean){
    this->horizon = horizon;
    this->current_time = current_time;
    this->active_server_A = active_server_A;
    this->active_server_B = active_server_B;
    this->active_server_C = active_server_C;
    this->dimmer_level = dimmer_level;
    this->traffic_A = traffic_A;
    this->traffic_B = traffic_B;
    this->traffic_C = traffic_C;
    this->boot_remain = boot_remain;
    this->boot_delay = boot_delay;
    this->evaluation_period = evaluation_period;
    this->boot_type = boot_type;
    this->arrival_mean = arrival_mean;
}

void PlanDB::destroy_db() {
    clean_db();

    assert(m_db_object != NULL);
    delete m_db_object;

    m_db_object = NULL;
}

void PlanDB::clean_db() {
    /*AdversaryMap::iterator itr = m_adversary_map.begin();

    while (itr != m_adversary_map.end()) {
        delete itr->second;
        ++itr;
    }*/

    m_adversary_map.clear();
    m_state_hash_map.clear();
}

std::size_t PlanDB::get_hash(string state_str) {
    std::hash<std::string> hash_fn;
    std::size_t str_hash = hash_fn(state_str);

    return str_hash;
}

bool PlanDB::populate_states(const char* dir) {
    bool success = true;
    string states_path = dir + string("/") + m_states_file;
    ifstream states_fin(states_path);

    if (!states_fin) {
        cout << "Could not read input file " << states_path << endl;
        success = false;
    } else {
        string line;
        bool firstLine = true;

        while (getline(states_fin, line)) {
            if (firstLine) {
                firstLine = false;
                continue;
            }

            typedef boost::tokenizer<boost::char_separator<char> > tokenizer;
            tokenizer tokens(line, boost::char_separator<char>(":"));
            tokenizer::iterator it = tokens.begin();

            if (it != tokens.end()) {
                //int state = atoi(it->c_str());
                unsigned long state = ULONG_MAX;
                stringstream(*it) >> state;

                assert (++it != tokens.end());
                size_t hash = get_hash(*it);

                if (debug) {
                    //cout << "state = " << state << " hash = " << hash << endl;
                }

                m_state_hash_map[hash] = state;
            }
        }
    }

    states_fin.close();

    return success;
}

bool PlanDB::populate_adv(const char* dir) {
    bool success = true;
    string adv_path = dir + string("/") + m_adversary_file;
    ifstream adv_fin(adv_path);

    if (!adv_fin) {
        cout << "Could not read input file " << adv_path << endl;
        success = false;
    } else {
        string line;
        bool firstLine = true;
        bool secondLine = true;

        while (getline(adv_fin, line)) {
            if (firstLine) {
                firstLine = false;
                continue;
            }
            if (secondLine) {
                secondLine = false;
                continue;
            }

            typedef boost::tokenizer<boost::char_separator<char> > tokenizer;
            tokenizer tokens(line, boost::char_separator<char>(" "));
            tokenizer::iterator it = tokens.begin();

            // First token is current state
            unsigned long curr_state = ULONG_MAX; // = atoi(it->c_str());
            stringstream(*it) >> curr_state;
            //int curr_state = atoi(it->c_str());
            assert(++it != tokens.end());

            // Second token is next state
            unsigned long next_state = ULONG_MAX; // = atoi(it->c_str());
            stringstream(*it) >> next_state;
            assert(++it != tokens.end());

            // Third token is probability. Ignoring it for now.
            assert(++it != tokens.end());

            // Fourth token is the action
            TacticEnum action = get_tactic_code(*it);

            PlanDB::Transition transition = PlanDB::Transition(next_state, action);
            PlanDB::Transition transition1(next_state, action);
            m_adversary_map[curr_state] = transition;

            if (debug) {
                //cout << "state = " << curr_state << " next_state = " << next_state
                //        << " action_str = " << *it << " action = " << action << endl;
            }
        }
    }

    adv_fin.close();

    return success;
}

bool PlanDB::populate_db(const char* dir) {
    bool success = true;

    success = populate_states(dir);

    if (success) {
        success = populate_adv(dir);
    }

    return success;
}

TacticEnum PlanDB::get_tactic_code(string tactic_name) {
    TacticEnum tactic = NONE;

    if (tactic_name.compare("tick") == 0) {
        tactic = TICK;
    } else if (tactic_name.compare("addServerA_start") == 0) {
        tactic = ADD_SERVER_A_START;
    } else if (tactic_name.compare("addServerB_start") == 0) {
        tactic = ADD_SERVER_B_START;
    } else if (tactic_name.compare("addServerC_start") == 0) {
        tactic = ADD_SERVER_C_START;
    } else if (tactic_name.compare("removeServerA_start") == 0) {
        tactic = REMOVE_SERVER_A_START;
    } else if (tactic_name.compare("removeServerB_start") == 0) {
        tactic = REMOVE_SERVER_B_START;
    } else if (tactic_name.compare("removeServerC_start") == 0) {
        tactic = REMOVE_SERVER_C_START;
    } else if (tactic_name.compare("increaseDimmer_start") == 0) {
        tactic = INC_DIMMER;
    } else if (tactic_name.compare("decreaseDimmer_start") == 0) {
        tactic = DEC_DIMMER;
    } else if (tactic_name.compare("divert_100_0_0") == 0) {
        tactic = DIVERT_TRAFFIC_100_0_0;
    } else if (tactic_name.compare("divert_75_25_0") == 0) {
        tactic = DIVERT_TRAFFIC_75_25_0;
    } else if (tactic_name.compare("divert_75_0_25") == 0) {
        tactic = DIVERT_TRAFFIC_75_0_25;
    } else if (tactic_name.compare("divert_50_50_0") == 0) {
        tactic = DIVERT_TRAFFIC_50_50_0;
    } else if (tactic_name.compare("divert_50_0_50") == 0) {
        tactic = DIVERT_TRAFFIC_50_0_50;
    } else if (tactic_name.compare("divert_50_25_25") == 0) {
        tactic = DIVERT_TRAFFIC_50_25_25;
    } else if (tactic_name.compare("divert_25_75_0") == 0) {
        tactic = DIVERT_TRAFFIC_25_75_0;
    } else if (tactic_name.compare("divert_25_0_75") == 0) {
        tactic = DIVERT_TRAFFIC_25_0_75;
    } else if (tactic_name.compare("divert_25_50_25") == 0) {
        tactic = DIVERT_TRAFFIC_25_50_25;
    } else if (tactic_name.compare("divert_25_25_50") == 0) {
        tactic = DIVERT_TRAFFIC_25_25_50;
    } else if (tactic_name.compare("divert_0_100_0") == 0) {
        tactic = DIVERT_TRAFFIC_0_100_0;
    } else if (tactic_name.compare("divert_0_0_100") == 0) {
        tactic = DIVERT_TRAFFIC_0_0_100;
    } else if (tactic_name.compare("divert_0_75_25") == 0) {
        tactic = DIVERT_TRAFFIC_0_75_25;
    } else if (tactic_name.compare("divert_0_25_75") == 0) {
        tactic = DIVERT_TRAFFIC_0_25_75;
    } else if (tactic_name.compare("divert_0_50_50") == 0) {
        tactic = DIVERT_TRAFFIC_0_50_50;
    } else if (tactic_name.compare("progressA") == 0) {
        tactic = PROGRESS_A;
    } else if (tactic_name.compare("progressB") == 0) {
        tactic = PROGRESS_B;
    } else if (tactic_name.compare("progressC") == 0) {
        tactic = PROGRESS_C;
    } else if (tactic_name.compare("addServerA_complete") == 0) {
        tactic = ADD_SERVER_A_COMPLETE;
    } else if (tactic_name.compare("addServerB_complete") == 0) {
        tactic = ADD_SERVER_B_COMPLETE;
    } else if (tactic_name.compare("addServerC_complete") == 0) {
        tactic = ADD_SERVER_C_COMPLETE;
    }

    return tactic;
}

string PlanDB::get_tactic_str(TacticEnum tactic) {
    string tactic_str = "";

    switch (tactic) {
    case TICK:
        tactic_str = "tick";
        break;
    case ADD_SERVER_A_START:
        tactic_str = "addServerA_start";
        break;
    case ADD_SERVER_B_START:
        tactic_str = "addServerB_start";
        break;
    case ADD_SERVER_C_START:
        tactic_str = "addServerC_start";
        break;
    case REMOVE_SERVER_A_START:
        tactic_str = "removeServerA_start";
        break;
    case REMOVE_SERVER_B_START:
        tactic_str = "removeServerB_start";
        break;
    case REMOVE_SERVER_C_START:
        tactic_str = "removeServerC_start";
        break;
    case INC_DIMMER:
        tactic_str = "increaseDimmer_start";
        break;
    case DEC_DIMMER:
        tactic_str = "decreaseDimmer_start";
        break;
    case DIVERT_TRAFFIC_100_0_0:
        tactic_str = "divert_100_0_0";
        break;
    case DIVERT_TRAFFIC_75_25_0:
        tactic_str = "divert_75_25_0";
        break;
    case DIVERT_TRAFFIC_75_0_25:
        tactic_str = "divert_75_0_25";
        break;
    case DIVERT_TRAFFIC_50_50_0:
        tactic_str = "divert_50_50_0";
        break;
    case DIVERT_TRAFFIC_50_0_50:
        tactic_str = "divert_50_0_50";
        break;
    case DIVERT_TRAFFIC_50_25_25:
        tactic_str = "divert_50_25_25";
        break;
    case DIVERT_TRAFFIC_25_75_0:
        tactic_str = "divert_25_75_0";
        break;
    case DIVERT_TRAFFIC_25_0_75:
        tactic_str = "divert_25_0_75";
        break;
    case DIVERT_TRAFFIC_25_50_25:
        tactic_str = "divert_25_50_25";
        break;
    case DIVERT_TRAFFIC_25_25_50:
        tactic_str = "divert_25_25_50";
        break;
    case DIVERT_TRAFFIC_0_100_0:
        tactic_str = "divert_0_100_0";
        break;
    case DIVERT_TRAFFIC_0_0_100:
        tactic_str = "divert_0_0_100";
        break;
    case DIVERT_TRAFFIC_0_75_25:
        tactic_str = "divert_0_75_25";
        break;
    case DIVERT_TRAFFIC_0_25_75:
        tactic_str = "divert_0_25_75";
        break;
    case DIVERT_TRAFFIC_0_50_50:
        tactic_str = "divert_0_50_50";
        break;
    case PROGRESS_A:
        tactic_str = "progressA";
        break;
    case PROGRESS_B:
        tactic_str = "progressB";
        break;
    case PROGRESS_C:
        tactic_str = "progressC";
        break;
    case ADD_SERVER_A_COMPLETE:
        tactic_str = "addServerA_complete";
        break;
    case ADD_SERVER_B_COMPLETE:
        tactic_str = "addServerB_complete";
        break;
    case ADD_SERVER_C_COMPLETE:
        tactic_str = "addServerC_complete";
        break;
    case NONE:
        break;
    }

    return tactic_str;
}

bool PlanDB::populate_state_obj(State& state) {
    //cout << "inside populate_state_obj\n";
    bool valid_state = false;

    state.m_s = UINT_MAX;
    

    if (true) {
        state.m_s = pladapt::timeseries::EnvPredictionRecord::get_instance()->getClosestArrivalRateIndex(
                    1 / arrival_mean, current_time);
    } 

    //cout << "yayeet\n";
    if (state.m_s != UINT_MAX) {
        state.m_time = current_time;
        state.m_readyToTick = true;

        state.m_active_servers_A = active_server_A;
        state.m_active_servers_B = active_server_B;
        state.m_active_servers_C = active_server_C;

        state.m_dimmer = dimmer_level;
        state.m_traffic_A = traffic_A;
        state.m_traffic_B = traffic_B;
        state.m_traffic_C = traffic_C;
        state.m_removeServer_go = true;
        state.m_removeServer_used = false;

        state.m_addServerA_state = 0;
        state.m_addServerB_state = 0;
        state.m_addServerC_state = 0;

        if (boot_remain > 0) {
            int bootPeriods = boot_delay
                    / evaluation_period;
            int addServerState = min(
                    bootPeriods - boot_remain,
                    bootPeriods);

            /*std::cout << "boot_remain = " << boot_remain << std::endl;
            std::cout << "boot_delay = " << boot_delay << std::endl;
            std::cout << "bootPeriods = " << bootPeriods << std::endl;
            std::cout << "evaluation_period = " << evaluation_period << std::endl;
            std::cout << "addServerState = " << addServerState << std::endl;
            std::cout << "boot_type = " << boot_type << std::endl;*/

            int bootType = boot_type;

            if (addServerState != 0) {
                switch (bootType) {
                case 1:
                    state.m_addServerA_state = addServerState;
                    break;
                case 2:
                    state.m_addServerB_state = addServerState;
                    break;
                case 3:
                    state.m_addServerC_state = addServerState;
                    break;
                default:
                    assert(false);
                }
            }
        }

        state.m_addServer_go = true;
        state.m_divert_go = true;
        state.m_increaseDimmer_go = true;
        state.m_increaseDimmer_used = false;
        state.m_decreaseDimmer_go = true;
        state.m_decreaseDimmer_used = false;
        valid_state = true;
    }

    return valid_state;
}

unsigned long PlanDB::get_state(State& state) {
    unsigned long state_no = ULONG_MAX;
    string state_str = state.get_state_str();

    if (debug) {
        cout << "State str" << state_str << endl;
    }

    size_t state_hash = get_hash(state_str);
    StateHashMap::iterator itr = m_state_hash_map.find(state_hash);

    if (itr != m_state_hash_map.end()) {
        state_no = (*itr).second;
    }

    return state_no;
}

bool PlanDB::get_plan() {
    //cout << "inside get_plan\n";
    PlanDB::Plan plan;
    bool plan_found = false;

    State state_obj;

    if (m_adversary_map.size() != 0 && (horizon > current_time)
            && populate_state_obj(state_obj)) {

        //cout << "inside if\n";
        unsigned long state = get_state(state_obj);
       // cout << "after get_state\n";

        if (debug) {
            cout << "state hash = " << state << endl;
        }
        if (state != ULONG_MAX) {
            AdversaryMap::iterator itr = m_adversary_map.find(state);
            assert(itr != m_adversary_map.end());
            unsigned long next_state = (*itr).second.first;
            string tactic = get_tactic_str((*itr).second.second);

            while (true) {
                if ((*itr).second.second == TICK) {
                    break;
                }

                if (tactic != "") {
                    if (debug) cout << "tactic = " << tactic << endl;
                    plan.push_back(tactic);
                }

                itr = m_adversary_map.find(next_state);
                assert(itr != m_adversary_map.end());
                next_state = (*itr).second.first;
                tactic = get_tactic_str((*itr).second.second);
            }
            plan_found = true;
            actions = plan;
        }
    }

    return plan_found;
}

//}
