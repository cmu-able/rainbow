## How to write an Unit Test for Probe

### Step I Test preparation: Initialize the mocked Rainbow, the mocked PortFactory and the GaugeDescription clase

Here is an example for setting a probe test. This works for **all probes**. For different probe, you need to specify their different configuration yaml file.  Your should put the input file of ***probe*** under the folder <path-to-your-testing>/resource/ 

```java
private File tempInput = extractResource("/blackhole/probe-input.txt");
public BlackholeProbeTest() throws IOException {
}
@Before
public void setUp() {
    RainbowMocker.injectRainbow();
    IRainbowConnectionPortFactory mockedPortFactory = mockConnectionPortFactory();
    stubPortFactoryForProbe(mockedPortFactory);
    RainbowMocker.injectPortFactory(mockedPortFactory);
}
```

### Step II Write test case for the *Probe*

1. Now your create the unit test 

   ```java
   @Test
   public void goodPath() {
   }
   ```

2. You can new a probe you want to test using the ***tempInput*** read from a file 

   ```java
   AbstractProbe probe = new BlackholeProbe("blackhole", 0L, new String[]{tempInput.toString()});
   ```

3. Start the ***Probe***

   ```java
   probe.create();
   probe.activate();
   ```

4. You can then compare the output with your ideal input

   ```java
   assertEquals("127.0.0.1, 1.0.0.1, 192.168.0.1", ProbeTestingUtil.waitForOutput());
   ```

### Here is how a complete unit test for gauge would look like:

```java
public class BlackholeProbeTest {

    private File tempInput = extractResource("/blackhole/probe-input.txt");

    public BlackholeProbeTest() throws IOException {
    }

    @Before
    public void setUp() {
        RainbowMocker.injectRainbow();
        IRainbowConnectionPortFactory mockedPortFactory = mockConnectionPortFactory();
        stubPortFactoryForProbe(mockedPortFactory);
        RainbowMocker.injectPortFactory(mockedPortFactory);
    }

    @Test
    public void goodPath() {
        AbstractProbe probe = new BlackholeProbe("blackhole", 0L, new String[]{tempInput.toString()});
        probe.create();
        probe.activate();
        assertEquals("127.0.0.1, 1.0.0.1, 192.168.0.1", ProbeTestingUtil.waitForOutput());
    }

}

```



### 