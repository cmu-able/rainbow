package org.sa.rainbow.brass.model.map.dijkstra;

import java.util.*;

/**
 * 
 * @author Michael Levet
 */
public class Dijkstra {
    
    private Graph graph;
    private String initialVertexLabel;
    private HashMap<String, String> predecessors;
    private HashMap<String, Float> distances; 
    private PriorityQueue<Vertex> availableVertices;
    private HashSet<Vertex> visitedVertices; 
    
    
    /**
     * This constructor initializes this Dijkstra object and executes
     * Dijkstra's algorithm on the graph given the specified initialVertexLabel.
     * After the algorithm terminates, the shortest a-b paths and the corresponding
     * distances will be available for all vertices b in the graph.
     * 
     * @param graph The Graph to traverse
     * @param initialVertexLabel The starting Vertex label
     * @throws IllegalArgumentException If the specified initial vertex is not in the Graph
     */
    public Dijkstra(Graph graph, String initialVertexLabel){
        this.graph = graph;
        Set<String> vertexKeys = this.graph.vertexKeys();
        
        if(!vertexKeys.contains(initialVertexLabel)){
            throw new IllegalArgumentException("The graph must contain the initial vertex.");
        }
        
        this.initialVertexLabel = initialVertexLabel;
        this.predecessors = new HashMap<String, String>();
        this.distances = new HashMap<String, Float>();
        this.availableVertices = new PriorityQueue<Vertex>(vertexKeys.size(), new Comparator<Vertex>(){
            
            public int compare(Vertex one, Vertex two){
                float weightOne = Dijkstra.this.distances.get(one.getLabel());
                float weightTwo = Dijkstra.this.distances.get(two.getLabel());
                
                if (weightOne<weightTwo){ 
                	return(-1);
                }
                if (weightOne>weightTwo){ 
                	return(1); 
                }
                return(0);               
//              return weightOne - weightTwo;
            }
        });
        
        this.visitedVertices = new HashSet<Vertex>();
        
        //for each Vertex in the graph
        //assume it has distance infinity denoted by Integer.MAX_VALUE
        for(String key: vertexKeys){
            this.predecessors.put(key, null);
            this.distances.put(key, Float.MAX_VALUE);
        }
        
        
        //the distance from the initial vertex to itself is 0
        this.distances.put(initialVertexLabel, 0.0f);
        
        //and seed initialVertex's neighbors
        Vertex initialVertex = this.graph.getVertex(initialVertexLabel);
        ArrayList<Edge> initialVertexNeighbors = initialVertex.getNeighbors();
        for(Edge e : initialVertexNeighbors){
            Vertex other = e.getNeighbor(initialVertex);
            this.predecessors.put(other.getLabel(), initialVertexLabel);
            this.distances.put(other.getLabel(), e.getWeight());
            this.availableVertices.add(other);
        }
        
        this.visitedVertices.add(initialVertex);
        
        //now apply Dijkstra's algorithm to the Graph
        processGraph();
        
    }
    
    /**
     * This method applies Dijkstra's algorithm to the graph using the Vertex
     * specified by initialVertexLabel as the starting point.
     * 
     * @post The shortest a-b paths as specified by Dijkstra's algorithm and 
     *       their distances are available 
     */
    private void processGraph(){
        
        //as long as there are Edges to process
        while(this.availableVertices.size() > 0){
            
            //pick the cheapest vertex
            Vertex next = this.availableVertices.poll();
            float distanceToNext = this.distances.get(next.getLabel());
            
            //and for each available neighbor of the chosen vertex
            List<Edge> nextNeighbors = next.getNeighbors();     
            for(Edge e: nextNeighbors){
                Vertex other = e.getNeighbor(next);
                if(this.visitedVertices.contains(other)){
                    continue;
                }
                
                //we check if a shorter path exists
                //and update to indicate a new shortest found path
                //in the graph
                float currentWeight = this.distances.get(other.getLabel());
                float newWeight = distanceToNext + e.getWeight();
                
                if(newWeight < currentWeight){
                    this.predecessors.put(other.getLabel(), next.getLabel());
                    this.distances.put(other.getLabel(), newWeight);
                    this.availableVertices.remove(other);
                    this.availableVertices.add(other);
                }
                
            }
            
            // finally, mark the selected vertex as visited 
            // so we don't revisit it
            this.visitedVertices.add(next);
        }
    }
    
    
    /**
     * 
     * @param destinationLabel The Vertex whose shortest path from the initial Vertex is desired
     * @return LinkedList<Vertex> A sequence of Vertex objects starting at the 
     *         initial Vertex and terminating at the Vertex specified by destinationLabel.
     *         The path is the shortest path specified by Dijkstra's algorithm.
     */
    public List<Vertex> getPathTo(String destinationLabel){
        LinkedList<Vertex> path = new LinkedList<Vertex>();
        path.add(graph.getVertex(destinationLabel));
        
        while(!destinationLabel.equals(this.initialVertexLabel)){
            Vertex predecessor = graph.getVertex(this.predecessors.get(destinationLabel));
            destinationLabel = predecessor.getLabel();
            path.add(0, predecessor);
        }
        return path;
    }
    
    
    /**
     * 
     * @param destinationLabel The Vertex to determine the distance from the initial Vertex
     * @return int The distance from the initial Vertex to the Vertex specified by destinationLabel
     */
    public float getDistanceTo(String destinationLabel){
        return this.distances.get(destinationLabel);
    }
    
    
    public static void main(String[] args){
        Graph graph = new Graph();
        Vertex[] vertices = new Vertex[6];
        
        for(int i = 0; i < vertices.length; i++){
            vertices[i] = new Vertex(i + "");
            graph.addVertex(vertices[i], true);
        }
        
        Edge[] edges = new Edge[9];
        edges[0] = new Edge(vertices[0], vertices[1], 7);
        edges[1] = new Edge(vertices[0], vertices[2], 9);
        edges[2] = new Edge(vertices[0], vertices[5], 14);
        edges[3] = new Edge(vertices[1], vertices[2], 10);
        edges[4] = new Edge(vertices[1], vertices[3], 15);
        edges[5] = new Edge(vertices[2], vertices[3], 11);
        edges[6] = new Edge(vertices[2], vertices[5], 2);
        edges[7] = new Edge(vertices[3], vertices[4], 6);
        edges[8] = new Edge(vertices[4], vertices[5], 9);
        
        for(Edge e: edges){
            graph.addEdge(e.getOne(), e.getTwo(), e.getWeight());
        }
        
        Dijkstra dijkstra = new Dijkstra(graph, vertices[0].getLabel());
        System.out.println(dijkstra.getDistanceTo("5"));
        System.out.println(dijkstra.getPathTo("5"));
    }
}

