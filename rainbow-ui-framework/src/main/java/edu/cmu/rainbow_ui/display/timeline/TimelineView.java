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
package edu.cmu.rainbow_ui.display.timeline;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.display.SystemViewProviderException;
import edu.cmu.rainbow_ui.display.widgets.IUpdatableWidget;

/**
 * This class defines the Timeline view
 *
 * <p>
 * The Timeline View consists of the timeline and all additional components needed to take advantage
 * of the timeline's full capabilities
 * </p>
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class TimelineView extends CustomComponent implements IUpdatableWidget {

    private final VerticalLayout mainLayout;
    private final HorizontalLayout buttonBar;
    private final HorizontalLayout timelineBar;
    protected final Panel timelinePanel;
    private final Timeline timeline;
    protected final Button playButton;
    protected final Button stopButton;
    protected final Button rewindButton;
    protected final ComboBox playRate;
    protected final CheckBox currentCheckBox;
    protected final CheckBox autoScrollCheckBox;
    protected final Button zoomInButton;
    protected final Button zoomOutButton;
    private double[] rates = {.001, .01, .1, .5, 1, 2, 4, 8, 16, 32};
    private boolean isActive;
    protected final float ZOOM_FACTOR = 2;
    private Date currentDate;
    private final ISystemViewProvider systemViewProvider;
    protected final DateField timeDisplay;
    protected boolean currentIsWrite;
    protected String readSession;

    {
        mainLayout = new VerticalLayout();
        buttonBar = new HorizontalLayout();

        playButton = new Button("Play");
        stopButton = new Button("Stop");
        playRate = new ComboBox();
        rewindButton = new Button("Rewind");

        autoScrollCheckBox = new CheckBox("AutoScroll");
        currentCheckBox = new CheckBox("Current");
        timeDisplay = makeTimeDisplay();
        zoomInButton = new Button("Zoom In");
        zoomOutButton = new Button("Zoom Out");

        timelineBar = new HorizontalLayout();
        timelinePanel = new Panel();
        currentIsWrite = true;
    }

    /**
     * Constructor for the timeline view
     *
     * @param systemViewProvider, the system view provider for the ui that the timeline view will be
     * associated with
     */
    public TimelineView(ISystemViewProvider systemViewProvider) {
        this.systemViewProvider = systemViewProvider;

        timeline = new Timeline(this, systemViewProvider.getStartDate());

        readSession = systemViewProvider.getSession();
        setUp();
    }

    /**
     * Constructor for testing the timeline view
     *
     * @param systemViewProvider, the mock acme system view provider for the ui that the timeline
     * view will be associated with
     * @param tlClass timeline class to create
     */
    TimelineView(ISystemViewProvider systemViewProvider,
            Class<? extends Timeline> tlClass) throws Exception {
        this.systemViewProvider = systemViewProvider;
        timeline = tlClass.getConstructor(TimelineView.class).newInstance(this);
        readSession = systemViewProvider.getSession();
        setUp();
    }

    /**
     * Arrange the components and add all listeners
     */
    private void setUp() {
        isActive = true;
        buttonBar.setWidth(100, Sizeable.Unit.PERCENTAGE);
        buttonBar.setSpacing(true);
        playButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                timeline.play();
                enableStop();
            }

        });
        buttonBar.addComponent(playButton);

        stopButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                timeline.stop();
                disableStop();
            }

        });
        buttonBar.addComponent(stopButton);

        rewindButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                timeline.rewind();
                enableStop();
            }

        });
        buttonBar.addComponent(rewindButton);

        Label playRateLabel = new Label("Play Rate");
        playRateLabel.setSizeUndefined();
        buttonBar.addComponent(playRateLabel);

        for (int i = 0; i < rates.length; i++) {
            playRate.addItem(rates[i] + "x");
            if (rates[i] == 1.0) {
                playRate.setValue(rates[i] + "x");
            }
        }
        playRate.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                timeline.setPlayRate(Double.parseDouble(playRate.getValue()
                        .toString().replace("x", "")));
            }
        });
        playRate.setNullSelectionAllowed(false);
        playRate.setSizeUndefined();
        buttonBar.addComponent(playRate);

        Label spacer = new Label("&nbsp;", ContentMode.HTML);
        spacer.setSizeUndefined();
        buttonBar.addComponent(spacer);
        buttonBar.setExpandRatio(spacer, 1.0f);

        autoScrollCheckBox.setValue(true);
        autoScrollCheckBox.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                if (autoScrollCheckBox.getValue()) {
                    timeline.enableAutoScroll();
                } else {
                    timeline.disableAutoScroll();
                }
            }

        });
        buttonBar.addComponent(autoScrollCheckBox);

        currentCheckBox.setValue(true);
        currentCheckBox.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                if (currentCheckBox.getValue()) {
                    timeline.setCurrent(true);
                    TimelineView.this.systemViewProvider.setUseCurrent();
                    timeDisplay.setEnabled(false);
                    autoScrollCheckBox.setEnabled(true);
                } else {
                    if (timeline.isCurrent()) {
                        timeline.setCurrent(false);
                    }
                    timeDisplay.setEnabled(true);
                    autoScrollCheckBox.setEnabled(false);
                    try {
                        timeline.setCurrentDate(timeDisplay.getValue());
                        TimelineView.this.systemViewProvider
                                .setUseHistorical(timeDisplay.getValue());
                    } catch (SystemViewProviderException ex) {
                        Notification.show(ex.toString());
                    }
                }
            }

        });
        buttonBar.addComponent(currentCheckBox);

        timeDisplay.setValue(timeline.getCurrentTime());
        timeDisplay.setEnabled(false);
        buttonBar.addComponent(timeDisplay);

        zoomInButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                int idealRange = (int) (timeline.getIdealRangeSize() * 1.0 / ZOOM_FACTOR);
                timeline.setIdealRange(idealRange);
            }

        });
        buttonBar.addComponent(zoomInButton);

        zoomOutButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                int idealRange = (int) (timeline.getIdealRangeSize() * ZOOM_FACTOR);
                timeline.setIdealRange(idealRange);
            }

        });
        buttonBar.addComponent(zoomOutButton);

        timelineBar.setWidth(100, Sizeable.Unit.PERCENTAGE);
        Label spacer1 = new Label("&nbsp;", ContentMode.HTML);
        timelineBar.addComponent(spacer1);
        timelineBar.setExpandRatio(spacer1, 1.0f);

        timelinePanel.setContent(timeline);
        timelinePanel.setWidth(1005, Sizeable.Unit.PIXELS);
        timelineBar.addComponent(timelinePanel);

        Label spacer2 = new Label("&nbsp;", ContentMode.HTML);
        timelineBar.addComponent(spacer2);
        timelineBar.setExpandRatio(spacer2, 1.0f);

        mainLayout.addComponent(buttonBar);
        mainLayout.addComponent(timelineBar);
        this.setCompositionRoot(mainLayout);

        currentDate = timeline.getCurrentTime();
    }

    /**
     * Updates the timeline view which will update the timeline
     */
    @Override
    public void update() {
        // If the session has switched
        if (!readSession.equals(systemViewProvider.getSession())) {
            timeline.setStartDate(systemViewProvider.getStartDate());
            setTimeDisplay(timeline.getStartDate());
            currentIsWrite = systemViewProvider.currentSessionIsWriteSession();
            readSession = systemViewProvider.getSession();
            if (!currentIsWrite) {
                timeline.setMaxDate(systemViewProvider.getMaxDate());
            }
        }

        /**
         * If the current read session is the write session, the timeline will update its maximum
         * time, otherwise it should be fixed
         */
        if (currentIsWrite) {
            if (timeline.isCurrent()) {
                timeline.setMaxDate(new Date());
                timeline.setCurrentDate(timeline.getMaxDate());
            } else {
                timeline.setMaxDate(new Date());
                if (timeline.getCurrentTime().getTime() != this.currentDate
                        .getTime()) {
                    this.currentDate = timeline.getCurrentTime();
                }
            }
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

    /**
     * Sets the timeline view to display the current state of the system
     */
    public void setCurrent(boolean isCurrent) {
        this.currentCheckBox.setValue(isCurrent);
    }

    private DateField makeTimeDisplay() {
        final DateField result = new DateField() {
            @Override
            protected Date handleUnparsableDateString(String dateString) {
                // Have a notification for the error
                Notification.show(
                        "Your date must be of the format MM/DD/YY hh:mm:ss AM",
                        Type.WARNING_MESSAGE);

                return currentDate;

            }
        };
        result.setResolution(Resolution.SECOND);
        result.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Date time = result.getValue();
                if (!TimelineView.this.currentCheckBox.getValue()) {
                    if (time.getTime() <= TimelineView.this.timeline
                            .getMaxDate().getTime()) {

                        boolean success = true;

                        /* If the date is after start date try to load it. */
                        if (time.getTime() >= TimelineView.this.timeline
                                .getStartDate().getTime()) {
                            try {
                                timeline.setCurrentDate(time);
                                TimelineView.this.systemViewProvider
                                        .setUseHistorical(time);
                            } catch (SystemViewProviderException ex) {
                                Logger.getLogger(TimelineView.class.getName()).log(Level.WARNING,
                                        "Cannot load historical data.", ex);
                                success = false;
                            }
                        } else {
                            success = false;
                        }

                        if (!success) {
                            String msg;
                            if (isCurrent()) {
                                msg = "The system is displaying the current state.";
                            } else {
                                msg = "The system is displaying the previous time:"
                                        + TimelineView.this.timeline.getCurrentTime();
                            }
                            Notification.show("There is no data available for a time: " + time + ". " + msg,
                                    Type.WARNING_MESSAGE);
                        }
                    } else {
                        Notification
                                .show("There is no data availabe for a time in the future. "
                                        + "The system is displaying the most recent time: "
                                        + TimelineView.this.timeline
                                        .getMaxDate(),
                                        Type.WARNING_MESSAGE);
                        TimelineView.this.timeDisplay
                                .setValue(TimelineView.this.timeline
                                        .getMaxDate());
                    }
                }
            }
        });
        return result;
    }

    /**
     * Set the time to display in the time display
     *
     * @param date the date to display
     */
    public void setTimeDisplay(Date date) {
        this.timeDisplay.setValue(date);
    }

    /**
     * Enable the autoscroll box
     */
    public void enableAutoScroll() {
        this.autoScrollCheckBox.setEnabled(true);
    }

    /**
     * disable the autoscroll box
     */
    public void disableAutoScroll() {
        this.autoScrollCheckBox.setEnabled(false);
    }

    /**
     * Get whether or not the current check box is checked or not, used for testing
     *
     * @return the value of the current check box
     */
    boolean isCurrent() {
        return this.currentCheckBox.getValue();
    }

    /**
     * Get the timeline used by the view, used for testing
     *
     * @return the timeline inside the this view
     */
    Timeline getTimeline() {
        return this.timeline;
    }

    public void disableStop() {
        stopButton.setEnabled(false);
    }

    public void enableStop() {
        stopButton.setEnabled(true);
    }

    public void setHistoricalDate(Date current) {
        timeDisplay.setValue(current);
    }

    public void setStartDate(Date startDate) {
        this.timeline.setStartDate(startDate);
    }

    /**
     * Set attached mode for the timeline - allow it to work with the current view.
     * 
     * Switches the state to current view.
     */
    public void setAttached() {
        currentCheckBox.setEnabled(true);
        currentCheckBox.setValue(true);
    }

    /**
     * Set detached mode for the timeline - forbid to set the current view.
     */
    public void setDetached() {
        currentCheckBox.setValue(false);
        currentCheckBox.setEnabled(false);
        currentIsWrite = false;
    }
}
