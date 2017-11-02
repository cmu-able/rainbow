package edu.cmu.rainbow_ui.display.event;

import java.util.ArrayList;
import java.util.List;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.tepi.filtertable.paged.PagedFilterTable;

import com.vaadin.data.util.IndexedContainer;

public class MockEventTable extends EventTable {
    
    public MockEventTable(List<IRainbowMessage> eventList) {
        super(eventList);
    }
    
    public MockEventTable() {
        super(new ArrayList<IRainbowMessage>());
    }

    public PagedFilterTable<IndexedContainer> getTable() {
        return super.filterTable;
    }
    
    public IndexedContainer getContainer() {
        return super.container;
    }
    
    public Object getSelectedItem() {
        return super.selectedItem;
    }
    
    public List<IRainbowMessage> getEvents () {
        return super.events;
    }
    
    public EventDetails getEventDetails () {
        return super.eventDetails;
    }

    @Override
    public void loadEvents() {
        
    }
}
