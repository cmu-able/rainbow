package edu.cmu.rainbow_ui.display;

import edu.cmu.rainbow_ui.display.ui.AbstractRainbowVaadinUI;
import edu.cmu.rainbow_ui.display.ui.Dashboard;

public class MockDashboard extends Dashboard {

    public MockDashboard(AbstractRainbowVaadinUI rui) {
        super(rui);
    }
    
    public AbstractRainbowVaadinUI getDashboardUI() {
        return super.ui;
    }
    
    public int getNumPages() {
        return super.tabs.size();
    }
}
