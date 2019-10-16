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
 * PlanDB.h
 *
 *  Created on: Aug 5, 2015
 *      Author: ashutosp
 */

#ifndef PLANDB_H_
#define PLANDB_H_


#include <string>
#include <vector>
#include <map>
//#include <omnetpp.h>
#include <pladapt/State.h>
#include "AllTactics.h"
//#include <HPModel.h>

//using namespace std;

// This singleton database class stores the current MDP plan i.e for long horizon
class PlanDB {
public:

    typedef std::pair<unsigned long, TacticEnum> Transition;
    typedef std::vector<std::string> Plan;
    typedef std::map<unsigned long, Transition> AdversaryMap;
    typedef std::map<size_t, unsigned long> StateHashMap;
    //typedef std::vector<Transition>AdversaryVector;

    /*struct Transition {
    public:
        Transition(unsigned long next_state, TacticEnum tactic):
            m_next_state (next_state),
            m_tactic (tactic) {
        }

        //unsigned m_row;
        unsigned long m_next_state;
        //float probability;
        TacticEnum m_tactic;
    };*/

    static PlanDB* get_instance() {
        if (m_db_object == NULL) {
            m_db_object = new PlanDB();
        }

        return m_db_object;
    }

    bool populate_db(const char* dir);
    void destroy_db();
    void clean_db();
    bool get_plan();

    //Plan get_actions(State& state);
    void update_val (int horizon,
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
    double arrival_mean);

    std::vector<std::string> actions;

private:
    PlanDB();
    ~PlanDB();

    size_t get_hash(string state_str);
    bool populate_state_obj(State& state);
    unsigned long get_state(State& state);
    string get_tactic_str(TacticEnum tactic);
    TacticEnum get_tactic_code(string tactic_name);
    bool populate_adv(const char* dir);
    bool populate_states(const char* dir);

    static PlanDB* m_db_object;
    string m_db_dir;

    AdversaryMap m_adversary_map;
    StateHashMap m_state_hash_map;

    const char* m_states_file;
    const char* m_adversary_file;

    int horizon;
    int current_time;
    int active_server_A;
    int active_server_B;
    int active_server_C;
    int dimmer_level;
    int traffic_A;
    int traffic_B;
    int traffic_C;
    int boot_remain;
    double boot_delay;
    double evaluation_period;
    int boot_type;
    double arrival_mean;
};

#endif /* PLANDB_H_ */
