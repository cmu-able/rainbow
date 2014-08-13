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
package edu.cmu.rainbow_ui.display.ui;

import com.vaadin.data.Property;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import edu.cmu.rainbow_ui.display.AcmeSystemViewProvider;
import edu.cmu.rainbow_ui.display.ApplicationCore;
import edu.cmu.rainbow_ui.display.config.ViewConfiguration;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class creates Menu bar on top of the screen.
 * 
 * <p>
 * The menu bar consists of time picker, view selector, alerts button, main menu
 * and search component
 * </p>
 * 
 * @author Anastasia Timoshenko <atimoshe@andrew.cmu.edu>
 */
public class MainMenuBar extends CustomComponent {

    private final AcmeSystemViewProvider svp;
    private final HorizontalLayout menuBar;
    private final MenuBar mainMenu;
    private final ComboBox viewSelector;
    private final MainLayout layout;
    private final AbstractRainbowVaadinUI ui;

    /**
     * Constructor for a Main menu bar component
     * 
     * @param rui AbstractRainbowVaadinUI instance
     * @param main MainLayout instance
     */
    public MainMenuBar(AbstractRainbowVaadinUI rui, MainLayout main) {
        layout = main;
        this.ui = rui;
        svp = (AcmeSystemViewProvider) ui.getSystemViewProvider();
        menuBar = new HorizontalLayout();
        menuBar.setWidth(100, Sizeable.Unit.PERCENTAGE);
        this.setCompositionRoot(menuBar);
        mainMenu = makeMainMenu();
        menuBar.addComponent(mainMenu);
        Label spacer = new Label("&nbsp;", ContentMode.HTML);
        spacer.setSizeUndefined();
        menuBar.addComponent(spacer);
        viewSelector = makeViewSelector();
        menuBar.addComponent(viewSelector);
        Label spacer2 = new Label("&nbsp;", ContentMode.HTML);
        spacer2.setSizeUndefined();
        menuBar.addComponent(spacer2);
        menuBar.setExpandRatio(spacer2, 1.0f);
        Label spacer3 = new Label("&nbsp;", ContentMode.HTML);
        spacer3.setSizeUndefined();
        menuBar.addComponent(spacer3);
        menuBar.setExpandRatio(spacer3, 1.0f);
        Label spacer4 = new Label("&nbsp;", ContentMode.HTML);
        spacer4.setSizeUndefined();
        menuBar.addComponent(spacer4);

    }

    /**
     * Creates Main Menu in Menu bar
     * 
     * @return MenuBar instance with Main Menu
     */
    private MenuBar makeMainMenu() {
        MenuBar result = new MenuBar();
        MenuBar.MenuItem menu = result.addItem("Menu", null);
        menuBar.addComponent(result);

        final int CONNECT = 0;
        final int DISCONNECT = 1;
        final MenuItem[] connectItems = new MenuItem[2];

        connectItems[CONNECT] = menu.addItem("Connect to Rainbow",
                new MenuBar.Command() {
                    @Override
                    public void menuSelected(MenuBar.MenuItem selectedItem) {
                        ApplicationCore.getInstance().attach();
                        ui.getSystemViewProvider().setSession(ApplicationCore.getInstance().getWriteSession());
                        layout.setUIattached();
                        connectItems[CONNECT].setEnabled(false);
                        connectItems[DISCONNECT].setEnabled(true);
                    }
                });
        connectItems[DISCONNECT] = menu.addItem("Disconnect from Rainbow",
                new MenuBar.Command() {
                    @Override
                    public void menuSelected(MenuBar.MenuItem selectedItem) {
                        ApplicationCore.getInstance().detach();
                        /* Force SVP update its session to historical(!= writeSessin) */
                        ui.getSystemViewProvider().setSession(ui.getSystemViewProvider().getSession());
                        layout.setUIdetached();
                        connectItems[DISCONNECT].setEnabled(false);
                        connectItems[CONNECT].setEnabled(true);
                    }
                });

        /**
         * Enable/disable menu items based on whether the system is initially
         * attached or not.
         */
        connectItems[CONNECT].setEnabled(!ApplicationCore.getInstance()
                .isAttached());
        connectItems[DISCONNECT].setEnabled(ApplicationCore.getInstance()
                .isAttached());

        menu.addItem("Load Session", new MenuBar.Command() {
            String session;

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                final Window window = new Window("Load Session");
                VerticalLayout content = new VerticalLayout();
                HorizontalLayout buttonrow = new HorizontalLayout();
                buttonrow.setWidth(100, Sizeable.Unit.PERCENTAGE);
                window.setContent(content);
                final ListSelect list = new ListSelect();
                ArrayList<String> sessions = svp.getSessionList();
                int id = sessions.indexOf(svp.getReadSession());
                list.addItems(sessions);
                list.select(id);
                content.addComponent(list);
                content.addComponent(buttonrow);
                Button okbutton = new Button("OK");
                buttonrow.addComponent(okbutton);
                Label spacer6 = new Label("&nbsp;", ContentMode.HTML);
                spacer6.setSizeUndefined();
                buttonrow.addComponent(spacer6);
                buttonrow.setExpandRatio(spacer6, 1.0f);
                Button cancelbutton = new Button("Cancel");
                buttonrow.addComponent(cancelbutton);
                list.addValueChangeListener(new Property.ValueChangeListener() {
                    @Override
                    public void valueChange(Property.ValueChangeEvent event) {
                        session = list.getValue().toString();
                    }
                });
                okbutton.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        svp.setSession(session);
                        window.close();
                    }
                });
                cancelbutton.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        window.close();
                    }
                });
                window.center();
                ui.addWindow(window);
            }
        });

        menu.addItem("Load View Configuration", new MenuBar.Command() {
            String configuration;

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                final Window window = new Window("Load View Configuration");
                VerticalLayout content = new VerticalLayout();
                HorizontalLayout buttonrow = new HorizontalLayout();
                buttonrow.setWidth(100, Sizeable.Unit.PERCENTAGE);
                window.setContent(content);
                final ListSelect list = new ListSelect();
                list.setWidth(100, Sizeable.Unit.PERCENTAGE);
                final ApplicationCore appCore = ApplicationCore.getInstance();
                ArrayList<String> configurations = appCore
                        .getViewConfigurations();
                list.addItems(configurations);
                list.setNullSelectionAllowed(false);
                content.addComponent(list);
                content.addComponent(buttonrow);
                Button okbutton = new Button("OK");
                buttonrow.addComponent(okbutton);
                Label spacer6 = new Label("&nbsp;", ContentMode.HTML);
                spacer6.setSizeUndefined();
                buttonrow.addComponent(spacer6);
                buttonrow.setExpandRatio(spacer6, 1.0f);
                Button cancelbutton = new Button("Cancel");
                buttonrow.addComponent(cancelbutton);
                list.addValueChangeListener(new Property.ValueChangeListener() {
                    @Override
                    public void valueChange(Property.ValueChangeEvent event) {
                        configuration = list.getValue().toString();
                    }
                });
                okbutton.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        ViewConfiguration vc = appCore
                                .getViewConfiguration(configuration);
                        if (vc != null) {
                            ui.setViewConfiguration(vc);
                        } else {
                            Logger.getLogger(MainMenuBar.class.getName()).log(
                                    Level.SEVERE,
                                    "Could not load the configuration: {0}",
                                    configuration);
                        }
                        layout.updateConfiguration();
                        window.close();
                    }
                });
                cancelbutton.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        window.close();
                    }
                });
                window.center();
                ui.addWindow(window);
            }
        });

        menu.addItem("Save View Configuration", new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                final Window window = new Window("Save View Configuration");
                VerticalLayout content = new VerticalLayout();
                HorizontalLayout buttonrow = new HorizontalLayout();
                buttonrow.setWidth(100, Sizeable.Unit.PERCENTAGE);
                window.setContent(content);
                final TextField name = new TextField();
                name.setInputPrompt("Enter name");
                content.addComponent(name);
                content.addComponent(buttonrow);
                Button okbutton = new Button("OK");
                buttonrow.addComponent(okbutton);
                Label spacer6 = new Label("&nbsp;", ContentMode.HTML);
                spacer6.setSizeUndefined();
                buttonrow.addComponent(spacer6);
                buttonrow.setExpandRatio(spacer6, 1.0f);
                Button cancelbutton = new Button("Cancel");
                buttonrow.addComponent(cancelbutton);
                okbutton.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        ApplicationCore appCore = ApplicationCore.getInstance();
                        ArrayList<String> configurations = appCore
                                .getViewConfigurations();
                        if (configurations.contains(name.getValue())) {
                            final Window confirmWindow = new Window("Something went wrong...");
                            VerticalLayout content = new VerticalLayout();
                            HorizontalLayout buttonrow = new HorizontalLayout();
                            buttonrow.setWidth(100, Sizeable.Unit.PERCENTAGE);
                            confirmWindow.setContent(content);
                            Label messageLabel = new Label(
                                    "Configuration "
                                            + name.getValue()
                                            + " entered already exists");
                            content.addComponent(messageLabel);
                            Button okbutton = new Button("OK");
                            okbutton.addClickListener(new ClickListener() {

                                @Override
                                public void buttonClick(ClickEvent event) {
                                    confirmWindow.close();
                                }
                                
                            });
                            buttonrow.addComponent(okbutton);
                            Label spacer7 = new Label("&nbsp;",
                                    ContentMode.HTML);
                            spacer7.setSizeUndefined();
                            buttonrow.addComponent(spacer7);
                            buttonrow.setExpandRatio(spacer7, 1.0f);
                            
                            //TODO: Implement overwrite
                            /*Button cancelbutton = new Button("Cancel");
                            buttonrow.addComponent(cancelbutton);*/
                            content.addComponent(buttonrow);
                            confirmWindow.center();
                            ui.addWindow(confirmWindow);
                        } else {
                            layout.saveViewConfiguration();
                            appCore.saveViewConfiguration(
                                    layout.getViewConfiguration(),
                                    name.getValue());
                            window.close();
                        }
                    }
                });
                cancelbutton.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        window.close();
                    }
                });
                window.center();
                ui.addWindow(window);
            }
        });

        return result;
    }

    /**
     * Creates View Selector in Menu bar
     * 
     * @return ComboBox ViewSelector
     */
    private ComboBox makeViewSelector() {
        final ComboBox result = new ComboBox();
        result.setDescription("Select View");
        result.setNullSelectionAllowed(false);
        result.addItem(MainContentType.ACME_GRAPH);
        result.addItem(MainContentType.EVENT_TABLE);
        result.setValue(MainContentType.ACME_GRAPH);
        result.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                layout.setMainContentArea(MainContentType.valueOf(result
                        .getValue().toString()));
            }
        });
        return result;
    }
}
