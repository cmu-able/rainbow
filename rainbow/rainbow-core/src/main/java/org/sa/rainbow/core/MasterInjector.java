package org.sa.rainbow.core;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Created by schmerl on 6/16/2016.
 */
public class MasterInjector extends AbstractModule{

    @Override
    protected void configure () {
        bind (IRainbowEnvironment.class).to(Rainbow.class).in (Singleton.class);
    }
}
