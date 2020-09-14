package org.sa.rainbow.gui;

import org.sa.rainbow.core.ports.IMasterCommandPort;

public interface IRainbowGUI {

	void display();

	void setMaster(IMasterCommandPort master);

}
