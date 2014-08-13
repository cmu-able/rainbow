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

import java.util.ArrayList;
import java.util.List;

import org.sa.rainbow.core.event.IRainbowMessage;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Event Details
 * 
 * <p>
 * The event details layout will display the entire IRainbowMessage that was
 * selected.  It will display the message as a list of key-value pairs
 * </p>
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class EventDetails extends VerticalLayout {

    /**
     * Constructor to create a new Event Details Layout
     */
    public EventDetails() {
    }

    /**
     * Sets the message that will be displayed in the view
     * 
     * @param keys a list of the keys
     * @param values a list of the values
     */
    public void setMessage(IRainbowMessage message) {
        this.removeAllComponents();

        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.addComponent(new Label("Selected Message"));
        Button closeBtn = new Button("X");
        closeBtn.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                closeEventDetails();
            }
        });

        headerRow.addComponent(closeBtn);
        this.addComponent(headerRow);
        
        List <String> propertyNames = message.getPropertyNames();
        for (String name : propertyNames) {
            HorizontalLayout row = new HorizontalLayout();

            row.addComponent(new Label(name));
            row.addComponent(new Label(message.getProperty(name).toString()));
            row.setSpacing(true);
            this.addComponent(row);
        }
    }

    /**
     * Closes the view by removing all components
     */
    public void closeEventDetails() {
        this.removeAllComponents();
    }
}
