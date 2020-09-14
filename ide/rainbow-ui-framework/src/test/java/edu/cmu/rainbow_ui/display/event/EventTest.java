/*
 * The MIT License
 *
 * Copyright 2014 CMU MSIT-SE Rainbow Team.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package edu.cmu.rainbow_ui.display.event;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.model.acme.AcmeModelOperation;
import org.tepi.filtertable.paged.PagedFilterTable;

import com.google.gwt.editor.client.Editor.Ignore;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

public class EventTest {

    private List<IRainbowMessage> eventList1;
    private List<IRainbowMessage> eventList2;

    @Before
    public void setUp() throws Exception {
        eventList1 = new ArrayList<>();

        long curTime = 0;

        for (int i = 0; i < 5; i++) {
            try {
                IRainbowMessage event = new RainbowESEBMessage();
                event.setProperty(IRainbowMessageFactory.EVENT_TYPE_PROP,
                        AcmeModelEventType.SET_PROPERTY_VALUE.toString());
                event.setProperty(AcmeModelOperation.PROPERTY_PROP,
                        "ZNewsSys.Server0.load");
                event.setProperty(AcmeModelOperation.VALUE_PROP, i);
                event.setProperty(ESEBConstants.MSG_SENT, curTime + "");
                curTime += 10;
                eventList1.add(event);
            } catch (Exception e) {

            }
        }

        eventList2 = new ArrayList<>();
        for (int i = 6; i < 12; i++) {
            try {
                IRainbowMessage event = new RainbowESEBMessage();
                event.setProperty(IRainbowMessageFactory.EVENT_TYPE_PROP,
                        AcmeModelEventType.SET_PROPERTY_VALUE.toString());
                event.setProperty(AcmeModelOperation.PROPERTY_PROP,
                        "ZNewsSys.Server0.load");
                event.setProperty(AcmeModelOperation.VALUE_PROP, i);
                event.setProperty(ESEBConstants.MSG_SENT, curTime + "");
                curTime += 10;
                eventList2.add(event);
            } catch (Exception e) {

            }
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void CreateTable() {
        MockEventTable table = new MockEventTable();
        assertEquals(false, table.isActive());
        assertNotNull(table.getTable());
        assertNotNull(table.getContainer());
        assertNotNull(table.getEventDetails());
    }

    @Test
    public void CreateTableWithEvents() {
        MockEventTable table = new MockEventTable(eventList1);
        assertEquals(false, table.isActive());
        assertNotNull(table.getTable());
        assertNotNull(table.getContainer());
        assertNotNull(table.getEventDetails());
        assertNotNull(table.getEvents());
        assertEquals(eventList1.size(), table.getEvents().size());
    }

    @Test
    public void SetEvents() {
        MockEventTable table = new MockEventTable();

        table.setEvents(eventList1);
        assertNotNull(table.getEvents());
        assertEquals(eventList1.size(), table.getEvents().size());

        IndexedContainer container = table.getContainer();
        assertNotNull(container);
        assertEquals(eventList1.get(0).getPropertyNames().size() + 1, container
                .getContainerPropertyIds().size());
    }

    @Test
    public void AddEvent() {
        MockEventTable table = new MockEventTable();

        table.addEvent(eventList1.get(0));
        assertNotNull(table.getEvents());
        assertEquals(1, table.getEvents().size());

        IndexedContainer container = table.getContainer();
        assertNotNull(container);
        assertEquals(eventList1.get(0).getPropertyNames().size() + 1, container
                .getContainerPropertyIds().size());
    }

    @Ignore
    @Test
    public void AddEventToList() {
        MockEventTable table = new MockEventTable(eventList1);
        table.setEvents(eventList1);
        table.addEvent(eventList2.get(0));
        assertNotNull(table.getEvents());
        assertEquals(eventList1.size() + 1, table.getEvents().size());

        IndexedContainer container = table.getContainer();
        assertNotNull(container);
        assertEquals(eventList1.get(0).getPropertyNames().size() + 1, container
                .getContainerPropertyIds().size());

        assertEquals(eventList2.get(0),
                table.getContainer()
                .getItem(table.getContainer().getIdByIndex(0))
                .getItemProperty("Raw Message").getValue());
    }

    @Test
    public void SelectEvent() {
        MockEventTable eventTable = new MockEventTable(eventList1);

        PagedFilterTable<IndexedContainer> table = eventTable.getTable();
        assertNotNull(table);

        IndexedContainer container = eventTable.getContainer();
        assertNotNull(container);
        assertEquals(container.size(), eventList1.size());

        table.setValue(container.getIdByIndex(0));
        Object selectedItem = eventTable.getSelectedItem();
        assertNotNull(selectedItem);

        IRainbowMessage message = (IRainbowMessage) table.getContainerProperty(
                selectedItem, "Raw Message").getValue();
        assertNotNull(message);
        assertEquals(message.getPropertyNames().size(), eventList1.get(0)
                .getPropertyNames().size());
        assertEquals(message.getProperty(AcmeModelOperation.VALUE_PROP),
                eventList1.get(eventList1.size() - 1).getProperty(AcmeModelOperation.VALUE_PROP));

        table.select(null);
        selectedItem = eventTable.getSelectedItem();
        assertNull(selectedItem);
    }

    @Test
    public void SelectEventDetails() {
        EventDetails details = new EventDetails();
        details.setMessage(eventList1.get(0));

        Iterator<Component> iterate = details.iterator();
        while (iterate.hasNext()) {
            Component c = iterate.next();
            if (c instanceof HasComponents) {
                Iterator<Component> rowIterate = ((HorizontalLayout) c)
                        .iterator();
                String key = "";
                String value = "";
                boolean isKeyValueRow = true;
                while (rowIterate.hasNext()) {
                    Component cc = rowIterate.next();
                    if (cc instanceof Label) {
                        if (key.equals("")) {
                            key = ((Label) cc).getValue();
                        } else {
                            value = ((Label) cc).getValue();
                        }
                    } else {
                        isKeyValueRow = false;
                    }
                }

                if (isKeyValueRow) {
                    assertEquals(eventList1.get(0).getProperty(key).toString(),
                            value);
                }
            }
        }
    }
}
