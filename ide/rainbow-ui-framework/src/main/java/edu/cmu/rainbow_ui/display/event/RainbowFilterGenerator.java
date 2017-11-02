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

import org.tepi.filtertable.FilterGenerator;

import com.vaadin.data.Container.Filter;
import com.vaadin.server.Page;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Notification;

public class RainbowFilterGenerator implements FilterGenerator {

    /**
     * Generates a new Filter for the property with the given ID, using the
     * Value object as basis for the filtering.
     * 
     * @param propertyId ID of the filtered property.
     * @param value Value entered by the user. Type of the value will depend on
     *        the type this property has in the underlying container. Date,
     *        Boolean and enum types will be provided as themselves. All other
     *        types of properties will result in a String-typed filtering value.
     * @return A generated Filter object, or NULL if you want to allow
     *         FilterTable to generate the default Filter for this property.
     */
    public Filter generateFilter(Object propertyId, Object value) {
        if ("timestamp".equals(propertyId)) {
            // create timestamp filter
        } else if ("channel".equals(propertyId)) {
            // create channel filter
        } else if ("message".equals(propertyId)) {
            // create message filter
        } else if ("delegate".equals(propertyId)) {
            // create delegate filter
        }

        // For other properties, use the default filter
        return null;
    }

    /**
     * Generates a new Filter for the property with the given ID, using the
     * Field object and its value as basis for the filtering.
     * 
     * @param propertyId ID of the filtered property.
     * @param originatingField Reference to the field that triggered this filter
     *        generating request.
     * @return A generated Filter object, or NULL if you want to allow
     *         FilterTable to generate the default Filter for this property.
     */
    public Filter generateFilter(Object propertyId, Field<?> originatingField) {
        return null; // use the default values
    }

    /**
     * Allows you to provide a custom filtering field for the properties as
     * needed.
     * 
     * @param propertyId ID of the property for for which the field is asked for
     * @return a custom filtering field OR null if you want to use the generated
     *         default field.
     */
    public AbstractField<?> getCustomFilterComponent(Object propertyId) {
        return null; // use the default values
    }

    /**
     * This method is called when a filter has been removed from the container
     * by the FilterTable.
     * 
     * @param propertyId ID of the property from which the filter was removed
     */
    public void filterRemoved(Object propertyId) {
        Notification n = new Notification("Filter removed from: " + propertyId,
                Notification.Type.TRAY_NOTIFICATION);
        n.setDelayMsec(800);
        n.show(Page.getCurrent());
    }

    /**
     * This method is called when a filter has been added to the container by
     * the FilterTable
     * 
     * @param propertyId ID of the property to which the filter was added
     * @param filterType Type of the filter (class)
     * @param value Value the filter was based on
     */
    public void filterAdded(Object propertyId,
            Class<? extends Filter> filterType, Object value) {
        Notification n = new Notification("Filter added to: " + propertyId,
                Notification.Type.TRAY_NOTIFICATION);
        n.setDelayMsec(800);
        n.show(Page.getCurrent());
    }

    /**
     * This method is called when Filter generator fails for any reason. You may
     * handle the error and return any Filter or null as the result.
     * 
     * @param reason Root exception
     * @param propertyId ID of the property to which the filter was to be added
     * @param value Value the filter was to be based on
     * @return A Filter or null
     */
    public Filter filterGeneratorFailed(Exception reason, Object propertyId,
            Object value) {
        return null; // do nothing
    }

}
