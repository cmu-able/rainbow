package org.sa.rainbow.gauges;

import java.util.List;

import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;

public interface IGaugeQueryInterface {
    /**
     * Returns the entire state of this Gauge via the supplied lists.
     * 
     * @param setupParams
     *            the list of setup type-name-value triples
     * @param configParams
     *            the list of configuration type-name-value triples
     * @param mappings
     *            the list of value-property mapping pairs
     * @return boolean <code>true</code> if query succeeds, <code>false</code> otherwise
     */
    public IGaugeState queryGaugeState ();

    /**
     * Queries for a value identified by the property name.
     * 
     * @param value
     *            the AttributeValueTriple object to contain the value
     * @return boolean <code>true</code> if query succeeds, <code>false</code> otherwise
     */
    public IRainbowModelCommandRepresentation queryLastCommand (String modelAttachment);

    /**
     * Queries for all of the values reported by this Gauge.
     * 
     * @param values
     *            the List of AttributeValueTriple values
     * @return boolean <code>true</code> if query succeeds, <code>false</code> otherwise
     */
    public List<IRainbowModelCommandRepresentation> queryAllCommands ();

}
