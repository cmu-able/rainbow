## How to write an Unit Test for Command

### Step I Test preparation: Initialize the mocked Rainbow

```java
@Before
public void setUp() {
    /*Initialze the RainbowMocker we provide to mock the rainbow instance*/
    RainbowMocker.injectRainbow();
}
```

### Step II Write test case for the ***Command***

#### 1. Construct Command from the CommandFactory for the target system

Here is an example for setting up the SetLoadCmd for ZNNCommandFactory. You should specify the acme file to load.

```java
StandaloneResource resource = StandaloneResourceProvider.instance().acmeResourceForString(
"src/test/resources/acme/znn.acme");
IAcmeSystem sys = resource.getModel().getSystems().iterator().next();
assertTrue(sys.getDeclaredTypes().iterator().next().isSatisfied());
ZNNModelUpdateOperatorsImpl znn = new ZNNModelUpdateOperatorsImpl(sys, "src/test/resources/acme/znn.acme");
IAcmeComponent server = sys.getComponent("s0");
/*For different type of command you call different 'set' method*/
AcmeModelOperation cns = znn.getCommandFactory().setLoadCmd(server, (float) 0.32);

```

#### 2. Run the SetLoadCmd

Here you need to use the mocked announce port we provide to execute the command. This should work for all the **commands** that need the **IModelChangeBusPort**. 

```java
/*Use the mockAnnouncePort*/
IModelChangeBusPort announcePort = mockAnnouncePort();
assertTrue(cns.canExecute());
List<? extends IRainbowMessage> generatedEvents = cns.execute(znn, announcePort);
```

#### 3. You can now assert and print the output

```java
assertTrue(cns.canUndo());
assertFalse(cns.canExecute());
assertFalse(cns.canRedo());
outputMessages(generatedEvents);
checkEventProperties(generatedEvents);
```

Here is an example function for printing the output.

```java
private void outputMessages(List<? extends IRainbowMessage> events) {
    for (IRainbowMessage msg : events) {
      System.out.println(msg.toString());
    }
}
```

Here is an example function to check the properties of the generated events.

```java
private void checkEventProperties(List<? extends IRainbowMessage> generatedEvents) {
  	assertTrue(generatedEvents.size() > 0);
 assertTrue(generatedEvents.iterator().next().getProperty(IModelChangeBusPort.EVENT_TYPE_PROP).equals(CommandEventT.START_COMMAND.name()));
  assertTrue(generatedEvents.get(generatedEvents.size() - 1).getProperty(IModelChangeBusPort.EVENT_TYPE_PROP).equals(CommandEventT.FINISH_COMMAND.name()));
    for (IRainbowMessage msg : generatedEvents) {
      assertTrue(msg.getPropertyNames().contains(ESEBConstants.MSG_TYPE_KEY));
    }
}
```





 

 
