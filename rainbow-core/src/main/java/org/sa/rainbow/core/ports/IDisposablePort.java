package org.sa.rainbow.core.ports;

public interface IDisposablePort {
    /**
     * Should be called when this port is no longer required. Implementors should dispose of all resources.
     */
    public abstract void dispose ();
}
