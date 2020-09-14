/*******************************************************************************
 * PLA Adaptation Manager
 *
 * Copyright 2017 Carnegie Mellon University. All Rights Reserved.
 * 
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING
 * INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON
 * UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED, AS
 * TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE
 * OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE
 * MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF ANY KIND
 * WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 *
 * Released under a BSD-style license, please see license.txt or contact
 * permission@sei.cmu.edu for full terms.
 *
 * [DISTRIBUTION STATEMENT A] This material has been approved for public release
 * and unlimited distribution. Please see Copyright notice for non-US Government
 * use and distribution.
 ******************************************************************************/

#include <pladapt/PRISMWrapper.h>

#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <fstream>
#include <string>
#include <boost/tokenizer.hpp>
#include <cstdlib>
#include <set>
#include <string.h>
#include <map>
#include <iostream>
#include <sstream>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <stdexcept>
#include <boost/filesystem.hpp>
#include <boost/scope_exit.hpp>

using namespace std;

namespace pladapt {

const char* PRISM = "prism";
const char* TACTIC_SUFFIX = "_start";

PRISMWrapper::PRISMWrapper() {
}

void PRISMWrapper::setModelTemplatePath(const std::string& modelTemplatePath) {
    // unless it's an absolute path, prefix it with .. because will be in the temp dir when this is used
    if (modelTemplatePath[0] == '/') {
        this->modelTemplatePath = modelTemplatePath;
    } else {
        this->modelTemplatePath = string("../") + modelTemplatePath;
    }
}

void PRISMWrapper::setPrismOptions(const std::vector<std::string>& options) {
	prismOptions = options;
}

bool PRISMWrapper::runPrism(const char* modelPath, const char* adversaryPath, const char* statesPath,
		const char* labelsPath, const char* pctl) {
	// TODO need to throw exceptions to provide better error handling
	pid_t pid = fork();
	if (pid == 0) {

		// create args vector
		std::vector<const char*> argv = { PRISM, modelPath, "-pctl", pctl,
				"-exportadv", adversaryPath, "-exportstates", statesPath,
				"-exportlabels", labelsPath };
		for (const auto& opt : prismOptions) {
			argv.push_back(opt.c_str());
		}
		argv.push_back(nullptr);

		// child
		// TODO perhaps we need to pipe the output to get errorr descriptions
		// see this url for example: http://www.cs.uleth.ca/~holzmann/C/system/pipeforkexec.html
		int status = execvp(PRISM, (char* const*) argv.data());
		if (status == -1) { // the only option really, otherwise execlp doesn't return
		    throw runtime_error(string("runPrism() execlp: ") + strerror(errno));
		}
	} else if (pid == -1) {
		throw runtime_error(string("runPrism() fork: ") + strerror(errno));
	}

	// parent
	int status;
	int rval = waitpid(pid, &status, 0);
	if (rval > 0 && WIFEXITED(status) && WEXITSTATUS(status) == 0) {
		return true;
	}

	return false;
}

/**
 * Finds the states that correspond to the current time.
 *
 * Assumption: time is the first variable in the state
 */
set<int> getNowStates(const char* statesPath) {
	// TODO throw exception on error (but be careful with the method that calls this one)

	/*
	 * Format:
	 * (time,s,env_turn,addServer_state,addServer_go,removeServer_state,removeServer_go,servers)
	 * 0:(0,0,false,0,false,0,true,1)
	 * 1:(0,0,false,0,true,0,true,1)
	 *
	 * From the second line on, the format is:
	 * <state>:(<value1>,<value2>,...)
	 */
	set<int> states;
	ifstream fin(statesPath);
	if (!fin) {
		cout << "Could not read input file " << statesPath << endl;
		return states;
	}

	string line;
	bool firstLine = true;
	while (getline(fin, line)) {
		if (firstLine) {
			firstLine = false;
			continue;
		}
		typedef boost::tokenizer<boost::char_separator<char> > tokenizer;
		tokenizer tokens(line, boost::char_separator<char>(":(,)"));
		tokenizer::iterator it = tokens.begin();
		if (it != tokens.end()) {
			int state = atoi(it->c_str());
			if (++it != tokens.end()) {
				if (*it == "0") {
					states.insert(state);
					//cout << "State @0: " << state << endl;
					continue;
				}
			}
		}
		break;
	}
	fin.close();

	return states;
}


/**
 * Finds the actions that take place in the given states
 *
 * Assumption: time is the first variable in the state
 */
struct AdversaryRow {
	int next;
	double probability;
	string action;
};

typedef map<int, AdversaryRow> AdversaryTable;

vector<string> getActions(const char* adversaryPath, const char* labelsPath,
		set<int>& states) {
	// TODO throw exception on error

	/*
	 * The format is like this:
	 * 4673 ?
	 * 0 3 1 clk
	 * 1 0 1 addServer_nostart
	 * 2 4 1
	 *
	 * From the second like on, <from state> <to state> <probability> [<action>]]
	 */


	vector<string> actions;
	typedef set<string> StringSet;
	StringSet actionSet;
	ifstream fin(adversaryPath);
	if (!fin) {
		cout << "Could not read input file " << adversaryPath << endl;
		return actions;
	}


	// first, load the adversary table for the states at time 0
	AdversaryTable table;

	size_t suffixLenght = strlen(TACTIC_SUFFIX);
	string line;
	bool firstLine = true;
	while (getline(fin, line)) {
		if (firstLine) {
			firstLine = false;
			continue;
		}
		typedef boost::tokenizer<boost::char_separator<char> > tokenizer;
		tokenizer tokens(line, boost::char_separator<char>(" "));
		tokenizer::iterator it = tokens.begin();
		if (it != tokens.end()) {
			int state = atoi(it->c_str());
			if (states.find(state) != states.end()) { // this is one of the relevant states
				AdversaryRow row;
				++it; // point to target state;
				row.next = atoi(it->c_str());
				++it; // point to the probability
				row.probability = atof(it->c_str());
				if (++it != tokens.end()) { // has action
					size_t actionLength = it->length();
					if (actionLength > suffixLenght) {
						if (it->compare(actionLength - suffixLenght, suffixLenght, TACTIC_SUFFIX) == 0) { // it's a tactic start action
							row.action = *it;
						}
					}
				}
				table.insert(pair<int, AdversaryRow>(state, row));
			}
		}
	}
	fin.close();

	// now find the initial state;
	/*
	 * assumptions:
	 * 	0 is "initial", and 0 is always the first label if it appears in a row
	 */
	int state = -1;
	ifstream labels(labelsPath);
	if (!labels) {
		cout << "Could not read input file " << labelsPath << endl;
		return actions;
	}
	firstLine = false;
	while (getline(labels, line)) {
		if (firstLine) {
			firstLine = false;
			continue;
		}
		typedef boost::tokenizer<boost::char_separator<char> > tokenizer;
		tokenizer tokens(line, boost::char_separator<char>(": "));
		tokenizer::iterator it = tokens.begin();
		if (it != tokens.end()) {
			int rowState = atoi(it->c_str());
			if (++it != tokens.end()) {
				int labelNumber = atoi(it->c_str());
				if (labelNumber == 0) {
					state = rowState;
					break;
				}
			}
		}
	}
	labels.close();

	/*
	 * Now walk the table from the initial state collecting the actions
	 * If we get to a point where the transition has prob < 1, we stop
	 * because it is probably the environment making a stochastic transition,
	 * which means that the system turn ended
	 */
	AdversaryTable::iterator rowIt;
	while ((rowIt = table.find(state)) != table.end()) {
		AdversaryRow& row = rowIt->second;
		if (!row.action.empty()) {
			actions.push_back(row.action);
		}
		if (row.probability < 1.0) {
			break;;
		}
		state = row.next;
	}

	return actions;
}

bool PRISMWrapper::generateModel(string environmentModel, string initialState, const char* modelPath) {
	const string ENVIRONMENT_TAG = "//#environment";
	const string INIT_TAG = "//#init";

	ofstream fout(modelPath);
	if (!fout) {
		cout << "Could not write output file " << modelPath << endl;
		return false;
	}

	ifstream fin(modelTemplatePath.c_str());
	if (!fin) {
		cout << "Could not read input file " << get_current_dir_name() << '/' << modelTemplatePath << endl;
		cout << "Error is: " << strerror(errno) << endl;
		cout << "Retrying..." << endl;
	    fin.open(modelTemplatePath.c_str(), ifstream::in);
	    if (!fin) {
	        cout << "Could not read input file " << get_current_dir_name() << '/' << modelTemplatePath << endl;
	        cout << "Error is: " << strerror(errno) << endl;
	        return false;
	    }
		cout << "it worked!" << endl;
	}

	string line;
	while (getline(fin, line)) {
		if (line == ENVIRONMENT_TAG) {
			fout << environmentModel << endl;
		} else if (line == INIT_TAG) {
			fout << initialState <<  endl;
		} else {
			fout << line << endl;
		}
	}

	fin.close();
	fout.close();
	return true;
}

std::vector<std::string> PRISMWrapper::plan(const std::string& environmentModel, const std::string& initialState,
		const std::string& pctl, std::string* path) {
	const char* modelPath = "ttimemodel.prism";
	const char* adversaryPath = "result.adv";
	const char* statesPath = "result.sta";
	const char* labelsPath = "result.lab";

	// create temp directory
	char tempDirTemplate[] = "modelXXXXXX";
	char* tempDir = mkdtemp(tempDirTemplate);
	if (!tempDir) {
		throw runtime_error("error PRISMWrapper::plan mkdtemp");
	}

	// save current dir and chdir into the temp
	int currentDir = open(".", O_RDONLY);
	if (currentDir == -1) {
		throw runtime_error("error PRISMWrapper::plan chdir");
	}
	if (chdir(tempDir) != 0) {
		close(currentDir);
		throw runtime_error("error PRISMWrapper::plan chdir");
	}
	vector<string> actions;

	{
		BOOST_SCOPE_EXIT(&path, &tempDir, &modelPath, &adversaryPath, &statesPath, &labelsPath, &currentDir) {
	        // restore current dir and remove temp if needed
	        if (path) {
	            path->assign(tempDir);
	        } else {
	            remove(modelPath);
	            remove(adversaryPath);
	            remove(statesPath);
	            remove(labelsPath);
	        }

	        if (fchdir(currentDir) == -1) {}; // nothing we can do about it
	        close(currentDir);

	        if (!path) {
	            rmdir(tempDir);
	        }
		} BOOST_SCOPE_EXIT_END

        // generate model
        /*
         * perhaps this could take a probability tree as an argument, and then generate the
         * prism model with the DTMC for the env and the right initial state, reflecting the
         * state of the system and running tactics
         *
         * The tactics should also be read from some input file, because the
         * latency information needs to be used in the model and also here to compute
         * the initial state reflecting the progress of the tactics.
         * Also, if we monitor that the tactic has not finished, even though it should have, we
         * can set the initial state so that it seems that it has one period to go.
         */

        if (generateModel(environmentModel, initialState, modelPath)) {
            if (runPrism(modelPath, adversaryPath, statesPath, labelsPath,
            		pctl.c_str())) {
            	if (boost::filesystem::exists(adversaryPath)) {
					set<int> states = getNowStates(statesPath);
					actions = getActions(adversaryPath, labelsPath, states);
            	} else {
            		throw std::domain_error("PRISM didn't generate an adversary");
            	}
            } else {
                throw runtime_error("error PRISMWrapper::plan runPrism");
            }
        } else {
            throw runtime_error("error PRISMWrapper::plan generateModel");
        }
	}

	return actions;
}

void PRISMWrapper::test() {
	set<int> states = getNowStates("test/s.sta");
	vector<string> actions = getActions("test/a.adv", "test/l.lab", states);
	for (unsigned i = 0; i < actions.size(); i++) {
		cout << "Action: " << actions[i] << endl;
	}
}

PRISMWrapper::~PRISMWrapper() {
	// TODO Auto-generated destructor stub
}

} // namespace
