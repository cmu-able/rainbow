package org.sa.rainbow.brass.model.map;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import org.sa.rainbow.core.models.ModelReference;


/**
 * Created by camara on 12/20/2016.
 */

public class EnvMap {
    private final float SAME_LOCATION_RADIUS = 1.75f;

    public EnvMap (ModelReference model) {
        m_model = model;
        m_last_insertion = new NodeInsertion();
        m_nodes = new HashMap<> ();
        m_new_node_id=0;
        m_arcs = new LinkedList<EnvMapArc> ();
        initWithSimpleMap(); // TODO: Substitute hardwired version of the map by one parsed from file
    }

    public ModelReference getModelReference () {
        return m_model;
    }

    public synchronized EnvMap copy () {
        EnvMap m = new EnvMap (m_model);
        m.m_nodes = new HashMap<String, EnvMapNode> (m_nodes);
        m.m_arcs = new LinkedList<EnvMapArc> (m_arcs);
        return m;
    }

    private Map<String, EnvMapNode> m_nodes;
    private LinkedList<EnvMapArc> m_arcs;
    private NodeInsertion m_last_insertion;
    private int m_new_node_id;

    private final ModelReference m_model;

    public synchronized LinkedList<EnvMapArc> getArcs () {
        return m_arcs;
    }

    public synchronized Map<String, EnvMapNode> getNodes () {
        return m_nodes;
    }

    public synchronized EnvMapNode getNode (double x, double y) {
        for (EnvMapNode node : m_nodes.values()) {
            if (node.getX() >= x - SAME_LOCATION_RADIUS && node.getX() <= x + SAME_LOCATION_RADIUS &&
                    node.getY() >= y - SAME_LOCATION_RADIUS && node.getY() <= y + SAME_LOCATION_RADIUS) return node;
        }

        return null;
    }

    public synchronized int getNodeCount () {
        return m_nodes.size();
    }

    public synchronized int getArcCount () {
        return m_arcs.size();
    }

    public synchronized void AddNode (String label, double x, double y) {
        m_nodes.put(label, new EnvMapNode(label, x, y, m_new_node_id));
        m_new_node_id++;
    }

    public synchronized void addArc (String source, String target, double distance, boolean enabled) {
        m_arcs.add(new EnvMapArc(source, target, distance, enabled));
    }

    public synchronized double getNodeX (String n) {
        return m_nodes.get(n).getX();
    }

    public synchronized double getNodeY (String n) {
        return m_nodes.get(n).getY();
    }

    public synchronized int getNodeId (String n) {
        return m_nodes.get(n).getId();
    }

    /**
     * Eliminates all arcs in map between nodes with labels na and nb
     * (independently of whether na is the source or the target node in the arc)
     * @param na String node a label
     * @param nb String node b label
     */
    public synchronized void removeArcs (String na, String nb) {
        ListIterator<EnvMapArc> iter = getArcs().listIterator();
        while(iter.hasNext()){
            if(iter.next().includesNodes(na, nb)) {
                iter.remove();
            }
        }
    }

    public static class NodeInsertion{
        protected String m_n;
        protected String m_na;
        protected String m_nb;
        protected double m_x;
        protected double m_y;


        public NodeInsertion copy () {
            NodeInsertion ni = new NodeInsertion();
            ni.m_n = m_n;
            ni.m_na = m_na;
            ni.m_nb = m_nb;
            ni.m_x = m_x;
            ni.m_y = m_y;
            return ni;
        }

    }

    public NodeInsertion getNodeInsertionResult () {
        return m_last_insertion.copy();
    }

    /**
     * Returns Euclidean distance between locations with labels na and nb
     * @param na String node label a
     * @param nb String node label b
     * @return float distance
     */
    public synchronized double distanceBetween (String na, String nb) {
        EnvMapNode a = m_nodes.get(na);
        EnvMapNode b = m_nodes.get(nb);
        double xc = Math.abs (a.getX () - b.getX ());
        double yc = Math.abs (a.getY () - b.getY ());
        return (float)Math.sqrt(xc*xc+yc*yc);		
    }

    /**
     * Inserts a new node in the map graph in between two nodes na and nb.
     * The arcs between the original endpoints are split, and the new pair of arcs between
     * the new node and nb are disabled (note that the order of na and nb in the invocation
     * of the method matters!).
     * @param n String label of the new node to insert
     * @param na String label of the node that the robot is moving away from
     * @param nb String label of the node that the robot is moving towards
     * @param x float coordinates of the location of the new node in the map
     * @param y
     */
    public synchronized void insertNode (String n, String na, String nb, double x, double y) {
        AddNode (n, x, y);
        removeArcs(na, nb);
        addArc (na, n, distanceBetween(na,n), true);
        addArc (n, na, distanceBetween(na,n), true);
        // Somehow, the planning things that n to nb is still valid
        //   addArc (nb, n, distanceBetween (nb, n), false);
        // addArc (n, nb, distanceBetween (nb, n), false);
    }

    public synchronized void initWithSimpleMap () {
        AddNode ("l1", 14.474, 69);
        AddNode ("l2", 19.82, 69);
        AddNode ("l3", 42.5, 69);
        AddNode ("l4", 52.22, 69);
        AddNode ("l5", 52.22, 58.74);
        AddNode ("l6", 42.5, 58.74);
        AddNode ("l7", 19.82, 58.74);
        AddNode ("l8", 19.82, 64.95);
        AddNode ("ls", 52.22, 74.4);

        addArc ("l1", "l2", 5.436, true);
        addArc ("l2", "l1", 5.436, true);
        addArc ("l2", "l3", 22.572, true);
        addArc ("l3", "l2", 22.572, true);
        addArc ("l3", "l4", 9.72, true);
        addArc ("l4", "l3", 9.72, true);
        addArc ("l2", "l8", 4.05, true);
        addArc ("l8", "l2", 4.05, true);
        addArc ("l8", "l7", 6.21, true);
        addArc ("l7", "l8", 6.21, true);
        addArc ("l7", "l6", 22.572, true);
        addArc ("l6", "l7", 22.572, true);
        addArc ("l3", "l6", 10.26, true);
        addArc ("l6", "l3", 3, true);
        addArc ("l4", "l5", 10.26, true);
        addArc ("l5", "l4", 10.26, true);
        addArc ("l6", "l5", 9.72, true);
        addArc ("l5", "l6", 9.72, true);
        addArc ("l4", "ls", 5.4, true);
        addArc ("ls", "l4", 5.4, true);
    }

}
