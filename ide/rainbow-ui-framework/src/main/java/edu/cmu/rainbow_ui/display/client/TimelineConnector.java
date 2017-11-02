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

package edu.cmu.rainbow_ui.display.client;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

import edu.cmu.rainbow_ui.display.timeline.Timeline;

/**
 * This class defines the Timeline component connector
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
@Connect(Timeline.class)
public class TimelineConnector extends AbstractComponentConnector {

    TimelineServerRpc rpc = RpcProxy.create(TimelineServerRpc.class, this);
    int xLoc;
    int xAbsLoc;
    int yAbsLoc;
    boolean isIn;
    boolean mouseIsDown;
    int mouseDownLoc;
    int scrollDownLoc;
    boolean scrollIsDown;

    /**
     * Constructor for the timeline connector which connects the server side and
     * client sides of the widget
     */
    public TimelineConnector() {
        super();
        isIn = false;
        getState();
        getWidget().setMaxDate(getState().maxDate);
        getWidget().setStartDate(getState().startDate);
        getWidget().setVisibleEndDate(getState().maxDate);
        getWidget().setVisibleStartDate(getState().startDate);
        getWidget().setIdealRangeSize(getState().idealRangeSize);
        getWidget().setCurrentDate(getState().currentDate);
        getWidget().drawTimeline();

        getWidget().getTimelineCanvas().addMouseOverHandler(
                new MouseOverHandler() {

                    @Override
                    public void onMouseOver(MouseOverEvent event) {
                        getWidget().createCurrentLocDateToolTip();
                    }

                });

        getWidget().getTimelineCanvas().addMouseDownHandler(
                new MouseDownHandler() {

                    @Override
                    public void onMouseDown(MouseDownEvent event) {
                        if (event.getRelativeElement() == getWidget()
                                .getElement()
                                && event.getRelativeY(getWidget().getElement()) < 30) {
                            mouseIsDown = true;
                            mouseDownLoc = event.getRelativeX(getWidget()
                                    .getElement());
                        } else if (event.getRelativeElement() == getWidget()
                                .getElement()
                                && event.getRelativeY(getWidget().getElement()) > 30) {
                            scrollIsDown = getWidget()
                                    .scrollPressed(
                                            event.getRelativeX(getWidget()
                                                    .getElement()));
                            if (scrollIsDown) {
                                scrollDownLoc = event.getRelativeX(getWidget()
                                        .getElement());
                            }
                        }

                    }

                });

        getWidget().getTimelineCanvas().addMouseMoveHandler(
                new MouseMoveHandler() {

                    @Override
                    public void onMouseMove(MouseMoveEvent event) {
                        if (event.getRelativeElement() == getWidget()
                                .getElement()
                                && event.getRelativeY(getWidget().getElement()) < 30) {
                            xLoc = event.getRelativeX(getWidget().getElement());
                            xAbsLoc = event.getClientX();
                            yAbsLoc = event.getClientY();
                            isIn = true;
                            getWidget().showCurrentLocDateToolTip(xLoc,
                                    xAbsLoc, yAbsLoc - 30);
                        }

                        if (mouseIsDown) {
                            int distance = event.getRelativeX(getWidget()
                                    .getElement()) - mouseDownLoc;
                            double offset = 0;
                            // If there is room to scroll update the offset
                            if ((distance > 0 && getWidget()
                                    .getVisibleStartDate().getTime() > getState().startDate
                                    .getTime())
                                    || (distance < 0 && getWidget()
                                            .getVisibleEndDate().getTime() < getState().maxDate
                                            .getTime())) {
                                offset = distance;
                            }

                            // If we drag an entire tick to the right
                            if (offset > 20) {
                                offset = 0;
                                mouseDownLoc = event.getClientX();
                                long increment = -(getWidget()
                                        .getVisibleEndDate().getTime() - getWidget()
                                        .getVisibleStartDate().getTime())
                                        / (getWidget().getNumTicks() - 1);
                                getWidget().setVisibleStartDate(
                                        new Date(getWidget()
                                                .getVisibleStartDate()
                                                .getTime()
                                                + increment));
                                getWidget().setVisibleEndDate(
                                        new Date(getWidget()
                                                .getVisibleEndDate().getTime()
                                                + increment));
                                mouseDownLoc += getWidget().getTimelineCanvas()
                                        .getWidth() / getWidget().getNumTicks();
                                getWidget().getTickMarks().remove(
                                        getWidget().getNumTicks() - 1);
                                getWidget().getTickMarks()
                                        .add(0,
                                                getWidget().getTickMarks().get(
                                                        3) ? true : false);
                            } else if (offset < -20) {
                                offset = 0;
                                mouseDownLoc = event.getClientX();
                                long increment = (getWidget()
                                        .getVisibleEndDate().getTime() - getWidget()
                                        .getVisibleStartDate().getTime())
                                        / (getWidget().getNumTicks() - 1);
                                getWidget().setVisibleStartDate(
                                        new Date(getWidget()
                                                .getVisibleStartDate()
                                                .getTime()
                                                + increment));
                                getWidget().setVisibleEndDate(
                                        new Date(getWidget()
                                                .getVisibleEndDate().getTime()
                                                + increment));
                                mouseDownLoc -= getWidget().getTimelineCanvas()
                                        .getWidth() / getWidget().getNumTicks();
                                getWidget().getTickMarks().remove(0);
                                getWidget()
                                        .getTickMarks()
                                        .add((getWidget().getTickMarks().get(
                                                getWidget().getNumTicks() - 5) ? true
                                                : false));
                            }
                            getWidget().setOffset(offset);
                            getWidget().drawTimeline();
                        }

                        if (scrollIsDown) {
                            getWidget()
                                    .scroll(event.getRelativeX(getWidget()
                                            .getElement()) - scrollDownLoc);
                            scrollDownLoc = event.getRelativeX(getWidget()
                                    .getElement());
                        }
                    }

                });

        getWidget().getTimelineCanvas().addMouseUpHandler(new MouseUpHandler() {

            @Override
            public void onMouseUp(MouseUpEvent event) {
                mouseIsDown = false;
            }

        });

        getWidget().getTimelineCanvas().addMouseOutHandler(
                new MouseOutHandler() {

                    @Override
                    public void onMouseOut(MouseOutEvent event) {
                        getWidget().hideCurrentLocDateToolTip();
                        isIn = false;
                    }

                });

        getWidget().getTimelineCanvas().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (event.getRelativeElement() == getWidget().getElement()
                        && event.getRelativeY(getWidget().getElement()) < 30) {
                    getWidget().setCurrentLoc(
                            event.getRelativeX(getWidget().getElement()));
                    rpc.setHistorical(getWidget().getCurrentDate());
                }
            }

        });
    }

    @Override
    public TimelineWidget getWidget() {
        return (TimelineWidget) super.getWidget();
    }

    @Override
    public TimelineState getState() {
        return (TimelineState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        getWidget().setMaxDate(getState().maxDate);
        if (getState().idealRangeSize > 0) {
            getWidget().setIdealRangeSize(getState().idealRangeSize);
        }
        if (getState().isCurrent) {
            getWidget().setCurrentDate(getState().currentDate);
            getWidget().setVisibleEndDate(getState().maxDate);
            Date visibleStartDate = new Date(getState().maxDate.getTime()
                    - getState().idealRangeSize);
            if (visibleStartDate.getTime() < getState().startDate.getTime()) {
                visibleStartDate = getState().startDate;
            }
            getWidget().setVisibleStartDate(visibleStartDate);
        } else {
            if (getState().maxDate.getTime() < getWidget()
                    .getVisibleStartDate().getTime()
                    + getState().idealRangeSize) {
                getWidget().setVisibleEndDate(getState().maxDate);
            }
        }
        if (isIn) {
            getWidget().showCurrentLocDateToolTip(xLoc, xAbsLoc, yAbsLoc - 30);
        }
        getWidget().drawTimeline();
    }
}
