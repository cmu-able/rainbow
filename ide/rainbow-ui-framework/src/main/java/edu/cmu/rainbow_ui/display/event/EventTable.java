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

/*
 * Adopted From: https://github.com/tepi/FilteringTable/
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.cmu.rainbow_ui.display.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.tepi.filtertable.FilterTable;
import org.tepi.filtertable.paged.PagedFilterControlConfig;
import org.tepi.filtertable.paged.PagedFilterTable;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable.RowHeaderMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.display.widgets.IUpdatableWidget;

/**
 * Event Table
 *
 * <p>
 * The event table will be used to display IRainbowMessages in a tabular form. It will allow for
 * sorting and filtering and will be updatable.
 * </p>
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class EventTable extends HorizontalLayout implements IUpdatableWidget {

    protected PagedFilterTable<IndexedContainer> filterTable;
    protected IndexedContainer container;
    protected Object selectedItem;
    protected boolean isActive;
    protected List<IRainbowMessage> events;
    protected EventDetails eventDetails;
    protected ISystemViewProvider systemViewProvider;
    protected Label updatesLabel;
    protected Button updatesButton;
    protected boolean isCurrent;
    protected PagedFilterControlConfig config;
    protected int numEvents;
    private Date historicalTime;

    /**
     * Constructor to create a new Event Table
     *
     * @param systemViewProvider the system view provider that will provide access to the events
     * list that will be displayed in the table
     */
    public EventTable(ISystemViewProvider systemViewProvider) {
        this.systemViewProvider = systemViewProvider;
        isActive = false;
        historicalTime = null; // Current time
        events = new ArrayList<>();
        buildContainer();
        createFilterTable();
    }

    /**
     * Constructor to create a new Event Table
     *
     * @param events the initial set of events that will be displayed in the table
     */
    public EventTable(List<IRainbowMessage> events) {
        isActive = false;
        buildContainer();
        createFilterTable();
        setEvents(events);
    }

    /**
     * Sets events that will be displayed in the table
     *
     * @param events the list of events to be displayed by the table
     */
    public void setEvents(List<IRainbowMessage> events) {
        this.events = new ArrayList<IRainbowMessage>();
        container.removeAllItems();
        for (int i = 0; i < events.size(); i++) {
            this.events.add(events.get(i));
            addEventToTable(events.get(i));
        }

        if (container.getContainerPropertyIds()
                .contains(ESEBConstants.MSG_SENT)) {
            container.sort(new Object[]{ESEBConstants.MSG_SENT},
                    new boolean[]{false});
        }
    }

    /**
     * Adds an event to the top of the table
     *
     * @param event the new event that is to be added
     */
    public void addEvent(IRainbowMessage event) {
        this.events.add(event);
        addEventToTable(event);
    }

    /**
     * Adds an event to the top of the table
     *
     * @param event the new event that is to be added
     */
    public void addEventToTable(IRainbowMessage event) {
        updateContainer(event);

        Object[] obj = new Object[container.getContainerPropertyIds().size()];
        Object[] cProps = container.getContainerPropertyIds().toArray();
        for (int i = 0; i < cProps.length; i++) {
            if (cProps[i].toString().equals("Raw Message")) {
                obj[i] = event;
            } else {
                obj[i] = event.getProperty(cProps[i].toString()).toString();
            }

        }

        /**
         * The code below just adds new element as a first element in the table. There is no
         * interface in filter table that will allow to do it easily.
         */
        try {
            Long itemId = new Long(Long.parseLong(event.getProperty(
                    ESEBConstants.MSG_SENT).toString()));
            Item newItem = this.filterTable.addItemAfter(null, itemId); // add
            // first
            if (newItem != null) {
                for (int i = 0; i < cProps.length; i++) {
                    newItem.getItemProperty(cProps[i]).setValue(obj[i]); // fill
                    // row
                }
                this.filterTable.addItem(obj, itemId); // set cells
            }
        } catch (NumberFormatException ex) {
            Logger.getLogger(EventTable.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates the filter table object which will display the events and sets up its parameters
     */
    private void createFilterTable() {
        VerticalLayout tableLayout = new VerticalLayout();
        HorizontalLayout updateRow = new HorizontalLayout();
        Label label = new Label("New Updates:");
        updatesLabel = new Label("");
        updatesButton = new Button("Update Table");
        updatesButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                loadEvents();
                filterTable.setPageLength(numEvents);
            }

        });
        updatesButton.setVisible(false);
        
        Button clearButton = new Button("Clear Table");
        clearButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                EventTable.this.setEvents(new ArrayList<IRainbowMessage>());
            }

        });
        clearButton.setVisible(true);

        updateRow.addComponent(label);
        updateRow.addComponent(updatesLabel);
        updateRow.addComponent(updatesButton);
        updateRow.addComponent(clearButton);
        updateRow.setSpacing(true);
        tableLayout.addComponent(updateRow);

        filterTable = new PagedFilterTable<IndexedContainer>();
        filterTable.setFilterDecorator(new RainbowFilterDecorator());
        filterTable.setFilterGenerator(new RainbowFilterGenerator());
        filterTable.setHeight(400, Sizeable.Unit.PIXELS);
        filterTable.setWidth(900, Sizeable.Unit.PIXELS);

        filterTable.setFilterBarVisible(true);
        filterTable.setSelectable(true);
        filterTable.setImmediate(true);
        filterTable.setMultiSelect(false);

        filterTable.setRowHeaderMode(RowHeaderMode.INDEX);
        filterTable.setColumnCollapsingAllowed(true);
        filterTable.setColumnReorderingAllowed(true);
        filterTable.setContainerDataSource(container);
        filterTable.setPageLength(10);

        filterTable
                .setVisibleColumns((Object[]) new String[]{"Raw Message"});
        filterTable.setColumnWidth("Message", 500);

        eventDetails = new EventDetails();

        filterTable.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                selectedItem = filterTable.getValue();
                if (selectedItem != null) {
                    eventDetails.setMessage((IRainbowMessage) filterTable
                            .getContainerProperty(selectedItem, "Raw Message")
                            .getValue());
                } else {
                    eventDetails.closeEventDetails();
                }
            }
        });

        this.setSpacing(true);
        this.setMargin(true);
        tableLayout.setSpacing(true);
        tableLayout.setMargin(true);

        config = new PagedFilterControlConfig();
        tableLayout.addComponent(filterTable
                .createControls(config));
        tableLayout.addComponent(buildButtons(filterTable));
        tableLayout.addComponent(filterTable);

        this.addComponent(tableLayout);
        this.addComponent(eventDetails);
        loadEvents();
    }

    /**
     * Creates the buttons that will be displayed below the table and used for control
     *
     * @param relatedFilterTable the table that will be associated with the control buttons that are
     * created
     */
    private Component buildButtons(final FilterTable relatedFilterTable) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setHeight(null);
        buttonLayout.setWidth("100%");
        buttonLayout.setSpacing(true);

        Label hideFilters = new Label("Show Filters:");
        hideFilters.setSizeUndefined();
        buttonLayout.addComponent(hideFilters);
        buttonLayout.setComponentAlignment(hideFilters, Alignment.MIDDLE_LEFT);

        for (Object propId : relatedFilterTable.getContainerPropertyIds()) {
            Component t = createToggle(relatedFilterTable, propId);
            buttonLayout.addComponent(t);
            buttonLayout.setComponentAlignment(t, Alignment.MIDDLE_LEFT);
        }

        CheckBox showFilters = new CheckBox("Toggle Filter Bar visibility");
        showFilters.setValue(relatedFilterTable.isFilterBarVisible());
        showFilters.setImmediate(true);
        showFilters.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                relatedFilterTable.setFilterBarVisible((Boolean) event
                        .getProperty().getValue());
            }
        });

        buttonLayout.addComponent(showFilters);
        buttonLayout.setComponentAlignment(showFilters, Alignment.MIDDLE_RIGHT);
        buttonLayout.setExpandRatio(showFilters, 1);

        CheckBox wrapFilters = new CheckBox("Wrap Filter Fields");
        wrapFilters.setValue(relatedFilterTable.isWrapFilters());
        wrapFilters.setImmediate(true);
        wrapFilters.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                relatedFilterTable.setWrapFilters((Boolean) event.getProperty()
                        .getValue());
            }
        });
        buttonLayout.addComponent(wrapFilters);
        buttonLayout.setComponentAlignment(wrapFilters, Alignment.MIDDLE_RIGHT);

        Button reset = new Button("Reset");
        reset.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                relatedFilterTable.resetFilters();
            }
        });
        buttonLayout.addComponent(reset);

        return buttonLayout;
    }

    /**
     * Creates the container that will hold the events
     */
    private void buildContainer() {
        container = new IndexedContainer();

        container.addContainerProperty("Raw Message", IRainbowMessage.class,
                null);
    }

    /**
     * Update the container to hold the IRainbowMessage
     */
    private void updateContainer(IRainbowMessage message) {
        List<String> propertyNames = message.getPropertyNames();
        for (String prop : propertyNames) {
            boolean exists = container.getContainerPropertyIds().contains(prop);
            if (!exists) {
                container.addContainerProperty(prop, String.class, null);
            }
        }
        filterTable.setContainerDataSource(container);
    }

    /**
     * Gets the event that is selected
     */
    public String getSelectedEvent() {
        Object rowId = filterTable.getValue();
        return (String) filterTable.getContainerProperty(rowId, "Message")
                .getValue();
    }

    /**
     * Sets the table to filter the specified column to the specified value
     *
     * @param column the column that the table will be filtered on
     * @param value the value that will be allowed in the column
     */
    public void setFilter(String column, String value) {
        filterTable.setFilterFieldValue(column, value);
    }

    /**
     * Creates toggles a specified column on whether or not their filters should be displayed
     *
     * @param relatedFilterTable the table for which the toggles will be created
     * @param propId the column for which the toggle will be created
     */
    private Component createToggle(final FilterTable relatedFilterTable,
            final Object propId) {
        CheckBox toggle = new CheckBox(propId.toString());
        toggle.setValue(relatedFilterTable.isFilterFieldVisible(propId));
        toggle.setImmediate(true);
        toggle.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                relatedFilterTable.setFilterFieldVisible(propId,
                        !relatedFilterTable.isFilterFieldVisible(propId));
            }
        });
        return toggle;
    }

    @Override
    public void update() {
        if (isActive) {
            numEvents = filterTable.getPageLength();
            if (systemViewProvider.isCurrent()) {
                if (historicalTime == null) {
                    /* Were current before */
                    this.updatesLabel.setValue(systemViewProvider
                            .getNewEventsCount() + "");
                    if (systemViewProvider.getNewEventsCount() > 0) {
                        updatesButton.setVisible(true);
                    } else {
                        updatesButton.setVisible(false);
                    }
                } else {
                    /* Just switched to current */
                    this.setEvents(systemViewProvider.getNumberOfEventsBefore(
                            new Date(), 100));
                    this.loadEvents();
                    updatesLabel.setVisible(true);
                }

            } else {
                if (historicalTime != systemViewProvider.getHistoricalTime()) {
                    /* Just switched to historical or historical point is different*/
                    isCurrent = false;
                    updatesButton.setVisible(false);
                    updatesLabel.setVisible(false);
                    this.setEvents(systemViewProvider.getHistoricalEvents());
                }
            }
            historicalTime = systemViewProvider.getHistoricalTime();
        }
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void activate() {
        isActive = true;
    }

    @Override
    public void deactivate() {
        isActive = false;
    }

    public void loadEvents() {
        List<IRainbowMessage> messages = systemViewProvider.getNewEvents();
        for (int i = 0; i < messages.size(); i++) {
            EventTable.this.addEvent(messages.get(i));
        }
        if (container.getContainerPropertyIds()
                .contains(ESEBConstants.MSG_SENT)) {
            container.sort(new Object[]{ESEBConstants.MSG_SENT},
                    new boolean[]{false});
        }
    }

}
