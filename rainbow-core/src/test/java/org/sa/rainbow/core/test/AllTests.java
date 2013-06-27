package org.sa.rainbow.core.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith (Suite.class)
@Suite.SuiteClasses ({ ESEBConnectionAndLifecycleSeparateVMTest.class, RainbowESEBConnectionAndLifecycleTest.class,
        RainbowLocalConnectionAndLifecycleTest.class })
public class AllTests {

}
