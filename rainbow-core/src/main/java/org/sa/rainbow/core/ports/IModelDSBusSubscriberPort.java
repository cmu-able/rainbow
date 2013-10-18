package org.sa.rainbow.core.ports;

public interface IModelDSBusSubscriberPort {

    public abstract void unsubscribeToOperations (IModelDSBusPublisherPort callback);

    public abstract void subscribeToOperations (IModelDSBusPublisherPort callback);

}
