package org.acmestudio.rainbow.ui.part;

import org.acmestudio.ui.view.element.ElementViewPart;
import org.acmestudio.ui.view.property.AbstractElementViewPartSectionAdapter;

public class GaugesSectionAdapter extends AbstractElementViewPartSectionAdapter {

    public GaugesSectionAdapter() {
        super ();
    }
    
    @Override
    public ElementViewPart createElementViewPart () {
        return new GaugesSection();
    }

}
