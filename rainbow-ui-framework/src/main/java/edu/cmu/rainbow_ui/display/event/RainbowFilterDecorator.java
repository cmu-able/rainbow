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

import java.text.DateFormat;
import java.util.Locale;

import org.tepi.filtertable.FilterDecorator;
import org.tepi.filtertable.numberfilter.NumberFilterPopupConfig;

import com.vaadin.server.Resource;
import com.vaadin.shared.ui.datefield.Resolution;

/**
 * Rainbow Filter Decorator
 * 
 * <p>
 * Specifies the format properties that will be used in the Event Table
 * </p>
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class RainbowFilterDecorator implements FilterDecorator {

    /**
     * Returns the filter display name for the given enum value when filtering
     * the given property id.
     * 
     * @param propertyId ID of the property the filter is attached to.
     * @param value Value of the enum the display name is requested for.
     * @return UI Display name for the enum value.
     */

    public String getEnumFilterDisplayName(Object propertyId, Object value) {
        return null; // use the default values
    }

    /**
     * Returns the filter icon for the given enum value when filtering the given
     * property id.
     * 
     * @param propertyId ID of the property the filter is attached to.
     * @param value Value of the enum the icon is requested for.
     * @return Resource for the icon of the enum value.
     */
    public Resource getEnumFilterIcon(Object propertyId, Object value) {
        return null; // no icons can be changed later if desired
    }

    /**
     * Returns the filter display name for the given boolean value when
     * filtering the given property id.
     * 
     * @param propertyId ID of the property the filter is attached to.
     * @param value Value of boolean the display name is requested for.
     * @return UI Display name for the given boolean value.
     */
    public String getBooleanFilterDisplayName(Object propertyId, boolean value) {
        return null; // use the default values
    }

    /**
     * Returns the filter icon for the given boolean value when filtering the
     * given property id.
     * 
     * @param propertyId ID of the property the filter is attached to.
     * @param value Value of boolean the icon is requested for.
     * @return Resource for the icon of the given boolean value.
     */
    public Resource getBooleanFilterIcon(Object propertyId, boolean value) {
        return null; // use the default values
    }

    /**
     * Returns whether the text filter should update as the user types. This
     * uses {@link TextChangeEventMode#LAZY}
     * 
     * @return true if the text field should use a TextChangeListener.
     */
    public boolean isTextFilterImmediate(Object propertyId) {
        return false; // can be changed on a per column basis
    }

    /**
     * The text change timeout dictates how often text change events are
     * communicated to the application, and thus how often are the filter values
     * updated.
     * 
     * @return the timeout in milliseconds
     */
    public int getTextChangeTimeout(Object propertyId) {
        return 750; // 0.75 seconds
    }

    /**
     * Return display caption for the From field
     * 
     * @return caption for From field
     */
    public String getFromCaption() {
        return null; // use the default values
    }

    /**
     * Return display caption for the To field
     * 
     * @return caption for To field
     */
    public String getToCaption() {
        return null; // use the default values
    }

    /**
     * Return display caption for the Set button
     * 
     * @return caption for Set button
     */
    public String getSetCaption() {
        return null; // use the default values
    }

    /**
     * Return display caption for the Clear button
     * 
     * @return caption for Clear button
     */
    public String getClearCaption() {
        return null; // use the default values
    }
    
    /**
     * Create a dateformat instance for the property 
     * 
     * @param propertyId ID of the property the resolution will be applied to
     */
    public DateFormat getDateFormat(Object propertyId) {
        return DateFormat.getDateInstance(DateFormat.LONG, new Locale("us",
            "US"));
    }

    /**
     * Return DateField resolution for the Date filtering of the property ID.
     * This will only be called for Date -typed properties. Filtering values
     * output by the FilteringTable will also be truncated to this resolution.
     * 
     * @param propertyId ID of the property the resolution will be applied to
     * @return A resolution defined in {@link DateField}
     */
    public Resolution getDateFieldResolution(Object propertyId) {
        return Resolution.SECOND; // use the smallest resolution possible
    }

    /**
     * Returns a date format pattern to be used for formatting the date/time
     * values shown in the filtering field of the given property ID. Note that
     * this is completely independent from the resolution set for the property,
     * and is used for display purposes only.
     * 
     * See SimpleDateFormat for the pattern definition
     * 
     * @param propertyId ID of the property the format will be applied to
     * @return A date format pattern or null to use the default formatting
     */
    public String getDateFormatPattern(Object propertyId) {
        return null;
    }

    /**
     * Returns the locale to be used with Date filters. If none is provided,
     * reverts to default locale of the system.
     * 
     * @return Desired locale for the dates
     */
    public Locale getLocale() {
        return new Locale("us", "US");
    }

    /**
     * Return the string that should be used as an "input prompt" when no
     * filtering is made on a filter component.
     * 
     * @return String to show for no filter defined
     */
    public String getAllItemsVisibleString()
      {
      return "Show all";
      }

    /**
     * Return configuration for the numeric filter field popup
     * 
     * @return Configuration for numeric filter
     */
    public NumberFilterPopupConfig getNumberFilterPopupConfig() {
        return null; // use the default values
    }

    /**
     * Defines whether a popup-style numeric filter should be used for the
     * property with the given ID.
     * 
     * The types Integer, Long, Float and Double are considered to be 'numeric'
     * within this context.
     * 
     * @param propertyId ID of the property the popup will be applied to
     * @return true to use popup-style, false to use a TextField
     */
    public boolean usePopupForNumericProperty(Object propertyId)
      {
      return true;
      }

}
