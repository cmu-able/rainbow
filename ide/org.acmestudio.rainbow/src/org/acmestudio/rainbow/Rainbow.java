package org.acmestudio.rainbow;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.acmestudio.rainbow.model.events.RainbowModelEventListener;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Rainbow extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.acmestudio.rainbow"; //$NON-NLS-1$

    // The shared instance
    private static Rainbow plugin;

    public static RainbowModelEventListener s_rainbowListener;

    /**
     * The constructor
     */
    public Rainbow() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        Bundle bundle = Platform.getBundle("org.mozilla.xulrunner");
        if (bundle != null) {
            URL resourceUrl = bundle.getResource("xulrunner");
            if (resourceUrl != null) {
                try {
                    URL fileUrl = FileLocator.toFileURL(resourceUrl);
                    File file = new File (fileUrl.toURI ());
                    System.setProperty("org.eclipse.swt.browser.XULRunnerPath", file.getAbsolutePath());
                }
                catch (IOException | URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        Rainbow.plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        Rainbow.plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Rainbow getDefault() {
        return Rainbow.plugin;
    }

    public static RainbowModelEventListener getRainbowListener () {
        return Rainbow.s_rainbowListener;
    }

}
