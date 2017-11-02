package org.acmestudio.rainbow.ui.part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.core.resource.RegionManager;
import org.acmestudio.acme.core.type.IAcmeFloatValue;
import org.acmestudio.acme.core.type.IAcmeRecordValue;
import org.acmestudio.acme.element.IAcmeElement;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.eclipse.core.util.LanguagePackHelper;
import org.acmestudio.rainbow.Rainbow;
import org.acmestudio.rainbow.ui.ShowSourceDialog;
import org.acmestudio.ui.EclipseHelper;
import org.acmestudio.ui.view.element.ElementViewPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.commands.IRainbowModelOperation;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class GaugesSection extends ElementViewPart {
    public static final String GAUGE_DESCRIPTION_TYPE = "GaugeDescriptionT";
    public static final String GAUGE_RESOURCE = "/resources/html/gauge-element.html";   
    
    protected static String s_html = null;

    public static class Filter implements IFilter {

        /**
         * Returns true if the object is an acme element and at least one of its
         * properties has a meta property with gauge information.
         */
        @Override
        public boolean select (Object toTest) {
            Object o = EclipseHelper.unwrapToElement(toTest);
            boolean applies = false;
            if (o instanceof IAcmeElementInstance) {
                IAcmeElementInstance element = (IAcmeElementInstance) o;
                Set<? extends IAcmeProperty> properties = element.getProperties();
                for (Iterator i = properties.iterator(); i.hasNext() && !applies;) {
                    IAcmeProperty prop = (IAcmeProperty) i.next();
                    Set<? extends IAcmeProperty> metaProps = prop.getProperties();
                    for (Iterator j = metaProps.iterator(); j.hasNext() && !applies;) {
                        IAcmeProperty metaProp = (IAcmeProperty) j.next();
                        if (metaProp.getType () != null && metaProp.getType().getName().endsWith(GAUGE_DESCRIPTION_TYPE))
                            applies = true;
                    }
                }

            }
            return applies;
        }
    }
    
    private static void initializeResource () {
        try {
            InputStream stream = GaugesSection.class.getResourceAsStream(GAUGE_RESOURCE);
            BufferedReader reader = new BufferedReader (new InputStreamReader(stream));
            String line = null;
            StringBuilder builder = new StringBuilder ();
            String ls = System.getProperty ("line.separator");
            while ((line = reader.readLine ()) != null) {
                builder.append (line);
                builder.append (ls);
            }
            
            s_html = builder.toString ();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private IAcmeElementInstance                         m_element;
    private Browser                                      m_swtBrowser;
    private Map<String, Set<IAcmeProperty>>              m_gaugedProperties    = new LinkedHashMap<>();
    private Map<String, Set<GaugeEventListener>> m_registeredListeners = Collections
                                                                                       .<String, Set<GaugeEventListener>> synchronizedMap(new HashMap<String, Set<GaugeEventListener>>());
    private AcmeModelInstance m_commandFactory; 
    private Action m_showSourceAction;
    public GaugesSection () {
        if (s_html == null)
            initializeResource();
    }
    
    
    @Override
    public void createControl (Composite parent, int style) {
        super.createControl(parent, style);
//        parent.setLayoutData(GridDataFactory.fillDefaults().align(GridData.FILL, GridData.FILL).grab(true, true).create());
        getComposite ().setLayout(new FillLayout());
        getComposite ().setBackground(org.eclipse.draw2d.ColorConstants.red);
        m_swtBrowser = new Browser(getComposite (), SWT.MOZILLA);
        m_swtBrowser.setJavascriptEnabled(true);
        
        // Add hook to allow updates
        new BrowserFunction(m_swtBrowser, "triggerUpdateValue") {
            @Override
            public Object function (Object[] arguments) {
                IAcmeProperty property = m_element.getProperty((String )arguments[0]);
                if (property != null) {
                    if (property.getProperty("operation") != null) {
                        IAcmeProperty operation = property.getProperty ("operation");
                        Map<String, Object> elements = PropertyHelper.toJavaVal((IAcmeRecordValue )operation.getValue());
                        List<String> args = (List<String> )elements.get("arguments");
                        AcmeModelCommandFactory cf = m_commandFactory.getCommandFactory();
                        ValuePromptDialog dialog = new ValuePromptDialog(GaugesSection.this.getComposite().getShell());
                        if (dialog.open() == IDialogConstants.OK_ID) {
                            String val = dialog.getValue ();
                        try {
                            IRainbowModelOperation cmd = cf.generateCommand((String )elements.get("name"), m_element.getQualifiedName(), val);
                            IModelUSBusPort announcePort = Rainbow.getRainbowListener().getAnnouncePort (ModelHelper.getAcmeSystem(m_element).getName ());
                            announcePort.updateModel(cmd);
                        }
                        catch (RainbowModelException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        }
                    }
                }
                return null;
            }
        };
        
        makeActions ();
    }

    @Override
    public void setInput (IAcmeElement o) {
        removeListeners();
        m_gaugedProperties.clear();
        m_registeredListeners.clear ();
        super.setInput(o);
        if (o instanceof IAcmeElementInstance) {
            m_element = (IAcmeElementInstance) o;
        }

        pullOutGaugeProperties();
        setupGaugeHtml ();
        addListeners();
        
        if (o != null) {
            IAcmeSystem sys = ModelHelper.getAcmeSystem(o);
            IAcmeProperty cfProp = sys.getProperty("rainbow-command-factory");
            if (cfProp != null) {
                String cf = (String )PropertyHelper.toJavaVal(cfProp.getValue());
                if (cf != null) {
                    if (m_commandFactory == null || !cf.equals(m_commandFactory.getClass ().getName ())) {
                        try {
                            Class<?> cfCls = Class.forName(cf);
                            Constructor init = cfCls.getConstructor(new Class[] {IAcmeSystem.class, String.class});
                            if (init != null) {
                                m_commandFactory = (AcmeModelInstance )init.newInstance(sys, "AcmeStudio");
                            }
                        }
                        catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                                | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void setupGaugeHtml () {
        if (m_element == null || m_gaugedProperties.isEmpty()) {
            m_swtBrowser.setText ("<p>There are no gauges for this element</p>");
            return;
        }
        m_swtBrowser.setText(s_html);
        
        m_swtBrowser.addProgressListener(new ProgressListener() {
            
            @Override
            public void completed (ProgressEvent event) {
//                m_swtBrowser.execute("test();");
                
                // Create all the gauges for the gauge properties
                for (Entry<String, Set<IAcmeProperty>> e : m_gaugedProperties.entrySet()) {
                    for (IAcmeProperty gaugeDescProp : e.getValue()) {
                        IAcmePropertyValue value = gaugeDescProp.getValue();
                        String propName = e.getKey();
                        if (value instanceof IAcmeRecordValue) {
                            IAcmeRecordValue gaugeDesc = (IAcmeRecordValue) value;
                            Object gaugeType = PropertyHelper.toJavaVal(gaugeDesc.getField("gauge-type").getValue());
                            if ("PressureGauge".equals(gaugeType)) {
                                createPressureGauge(propName, gaugeDesc);
                            }
                            else if ("TrinaryGauge".equals (gaugeType)) {
                                createTrinaryGauge (propName, gaugeDesc);
                            }
                            else if ("MovingLineGauge".equals(gaugeType))
                                createMovingLineGauge(propName, gaugeDesc);
                        }
                    }
                }
            }
            
            @Override
            public void changed (ProgressEvent event) {
                
            }
        });
        
        
        
       
        
    }

    private void createTrinaryGauge (String propName, IAcmeRecordValue gaugeDesc) {
        IAcmePropertyValue goodValueAcme = gaugeDesc.getField("good").getValue();
        IAcmePropertyValue badValueAcme = gaugeDesc.getField("bad").getValue();
        String good = "1";
        String bad = "-1";
        if (goodValueAcme != null) {
            try {
                good = LanguagePackHelper.defaultLanguageHelper().propertyValueToString(goodValueAcme, new RegionManager ());
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (badValueAcme != null) {
            try {
                bad = LanguagePackHelper.defaultLanguageHelper().propertyValueToString(badValueAcme, new RegionManager ());
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String name = (String )PropertyHelper.toJavaVal(gaugeDesc.getField ("name").getValue ());
        String scriptCommand = "createTrinaryGauge ('" + name + "','" + propName + "', '" + good + "', '" + bad + "');";
        m_swtBrowser.execute(scriptCommand);
        try {
            String script = "updateGauge ('" + name + "', '" + LanguagePackHelper.defaultLanguageHelper().propertyValueToString(m_element.getProperty(propName).getValue(), new RegionManager ()) +"');";
            m_swtBrowser.execute(script);
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void createPressureGauge (String propName, IAcmeRecordValue gaugeDesc) {
        IAcmeProperty gaugedProperty = m_element.getProperty(propName);
        IAcmePropertyValue minValueAcme = gaugeDesc.getField("min").getValue();
        IAcmePropertyValue maxValueAcme = gaugeDesc.getField("max").getValue ();
        IAcmePropertyValue redValueAcme = gaugeDesc.getField("red").getValue ();
        IAcmePropertyValue yellowValueAcme = gaugeDesc.getField ("yellow").getValue ();
        float minValue = minValueAcme instanceof IAcmeFloatValue?(float )PropertyHelper.toJavaVal(minValueAcme):(int)PropertyHelper.toJavaVal(minValueAcme);
        float maxValue = maxValueAcme instanceof IAcmeFloatValue?(float )PropertyHelper.toJavaVal(maxValueAcme):(int )PropertyHelper.toJavaVal(maxValueAcme);
        float red = -1;
        if (gaugeDesc.getField("red") != null && redValueAcme != null) {
            red = redValueAcme instanceof IAcmeFloatValue?(float )PropertyHelper.toJavaVal (redValueAcme):(int )PropertyHelper.toJavaVal(redValueAcme);
        }
        float yellow = -1;
        if (gaugeDesc.getField ("yellow") != null && yellowValueAcme != null) {
            yellow = yellowValueAcme instanceof IAcmeFloatValue?(float )PropertyHelper.toJavaVal(yellowValueAcme):(int )PropertyHelper.toJavaVal (yellowValueAcme);
        }
        String name = (String) PropertyHelper.toJavaVal(gaugeDesc.getField("name").getValue ());
        
        String scriptCmd = "createPressureGauge ('" + name + "', '" + propName + "', " + Float.toString (minValue) + ", " + Float.toString (maxValue);
        if (red != -1) {
            scriptCmd += ", " + Float.toString(red);
            if (yellow != -1) 
                scriptCmd += ", " + Float.toString (yellow);
        }
        
        if (gaugedProperty.getProperty("operation") != null) {
            scriptCmd += ", true";
        }
        
        scriptCmd += ");";
        m_swtBrowser.execute(scriptCmd);
        try {
            String script = "updateGauge ('" + name + "', " + LanguagePackHelper.defaultLanguageHelper().propertyValueToString(gaugedProperty.getValue(), new RegionManager ()) +");";
            m_swtBrowser.execute(script);
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    private static class TriggerUpdateValueFunction extends BrowserFunction {

        private final IAcmeElementInstance m_element;

        public TriggerUpdateValueFunction(Browser browser, IAcmeElementInstance element) {
            super(browser, "triggerUpdateValue");
            m_element = element;
        }
        
        @Override
        public Object function (Object[] arguments) {
            IAcmeProperty property = m_element.getProperty((String )arguments[0]);
            if (property != null) {
                
            }
            return null;
        }
        
    }
    
    private static class NextValueFunction extends BrowserFunction {

        protected static final String NEXT = "nextValueFor";
        private IAcmeProperty m_prop;
        
        public NextValueFunction(Browser swtBrowser, IAcmeProperty prop) {
            super(swtBrowser, NEXT + prop.getName().replaceAll("-", "_"));
            m_prop = prop;
        }
        
        @Override
        public Object function (Object[] arguments) {
            if (m_prop.getValue () == null)
                return null;
            return PropertyHelper.toJavaVal(m_prop.getValue (), Float.class);
        }
        
    }
    
    
    private void createMovingLineGauge (String propName, IAcmeRecordValue gaugeDesc) {
        IAcmePropertyValue minValueAcme = gaugeDesc.getField("min").getValue ();
        IAcmePropertyValue maxValueAcme = gaugeDesc.getField("max").getValue ();
        IAcmePropertyValue windowValueAcme = gaugeDesc.getField("window").getValue ();
        IAcmePropertyValue thresholdValueAcme = gaugeDesc.getField("threshold").getValue ();
        String name = (String )PropertyHelper.toJavaVal(gaugeDesc.getField("name").getValue ());
        
        float min = PropertyHelper.toJavaVal(minValueAcme, Float.class);
        float max = PropertyHelper.toJavaVal(maxValueAcme, Float.class);
        float threshold = PropertyHelper.toJavaVal(thresholdValueAcme, Float.class);
        
        int window = windowValueAcme!=null?PropertyHelper.toJavaVal(windowValueAcme, Integer.class):10;
        
        IAcmeProperty prop = m_element.getProperty(propName);
        NextValueFunction nvf = new NextValueFunction (m_swtBrowser, prop);
        
        String params = "{threshold:" + threshold + "}";
        String script = "createMovingLineGauge('" + name + "', '" + propName + "', " + Float.toString (min) + ", " + Float.toString (max) + ", " + window + ", " + nvf.getName () + ", " + params + ");";
        m_swtBrowser.execute(script);
        
    }

    private void removeListeners () {
        if (m_element == null)
            return;
        for (Entry<String,Set<GaugeEventListener>> entry : m_registeredListeners.entrySet()) {
            IAcmeProperty property = m_element.getProperty(entry.getKey ());
            for (GaugeEventListener l : entry.getValue()) {
                property.removeEventListener(l);
            }
        }
    }

    /**
     * Adds a listener to each property that fires when the value changes to
     * update the gauge
     */
    private void addListeners () {
        if (m_element == null)
            return;
        for (Entry<String,Set<IAcmeProperty>> entry : m_gaugedProperties.entrySet()) {
            IAcmeProperty property = m_element.getProperty(entry.getKey ());
            Set<GaugeEventListener> listeners = new HashSet<> ();
            for (IAcmeProperty gd : entry.getValue()) {
                GaugeEventListener eu = new GaugeEventListener (property, gd, m_swtBrowser);
                listeners.add (eu);
                property.addEventListener(eu);
            }
            
        }
        
        m_swtBrowser.addDisposeListener(new DisposeListener() {
            
            @Override
            public void widgetDisposed (DisposeEvent e) {
                removeListeners();
            }
        });
    }

    private void pullOutGaugeProperties () {
        if (m_element == null)
            return;
        Set<? extends IAcmeProperty> properties = m_element.getProperties();
        for (IAcmeProperty prop : properties) {
            Set<IAcmeProperty> gaugeDescs = pullOutGaugeDescriptions(prop);
            if (!gaugeDescs.isEmpty())
                m_gaugedProperties.put(prop.getName(), gaugeDescs);
        }
    }

    private Set<IAcmeProperty> pullOutGaugeDescriptions (IAcmeProperty prop) {
        Set<IAcmeProperty> gaugeDescs = new LinkedHashSet<>();
        for (IAcmeProperty p : prop.getProperties()) {
            if (p.getType().getName().endsWith(GAUGE_DESCRIPTION_TYPE))
                gaugeDescs.add(p);
        }
        return gaugeDescs;
    }
    
    @Override
    public void dispose () {
        super.dispose ();
        if (m_swtBrowser != null) m_swtBrowser.dispose();
        m_swtBrowser = null;
        removeListeners();
    }


    private void makeActions () {
        m_showSourceAction = new Action () {
            @Override
            public void run () {
                ShowSourceDialog dialog = new ShowSourceDialog (Display.getDefault().getActiveShell(), m_swtBrowser);
                dialog.open ();
            }
        };
        m_showSourceAction.setText ("Show source");
    }
    
    @Override
    public IContributionItem[] getContributions () {
        return new ActionContributionItem [] {new ActionContributionItem(m_showSourceAction)};
    }


    @Override
    public void addItemSelectionListener (SelectionListener listener) {
        
    }
    
    

}
