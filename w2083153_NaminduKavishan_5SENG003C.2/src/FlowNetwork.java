
import java.util.ArrayList;
import java.util.List;

/**
 * Student ID: 20230945
 * Westminster ID: w2083153
 * Name: Namindu Kavishan
 * University of Westminster - 5SENG003C.2
 */
public class FlowNetwork {
    private final int vertices;                   // Number of vertices (nodes) in the graph
    private final List<List<Edge>> adj;           // Adjacency list, where each node points to a list of edges

    /**
     * Constructor to initialize the flow network with a given number of vertices.
     * The number of vertices must be at least 2, representing source and sink nodes.
     *
     * @param vertices The number of nodes in the network
     */
    public FlowNetwork(int vertices) {
        if (vertices < 2) {
            throw new IllegalArgumentException("Network must have at least 2 vertices (source and sink)");
        }
        this.vertices = vertices;
        this.adj = new ArrayList<>(vertices);

        // Initialize the adjacency list for each vertex
        for (int i = 0; i < vertices; i++) {
            adj.add(new ArrayList<>());
        }
    }

    /**
     * Returns the number of vertices in the flow network.
     *
     * @return the number of vertices
     */
    public int getVertices() {
        return vertices;
    }

    /**
     * Returns the list of edges (adjacency list) for a given vertex.
     *
     * @param v the vertex index
     * @return the list of edges associated with vertex v
     * @throws IllegalArgumentException if the vertex index is invalid
     */
    public List<Edge> getAdj(int v) {
        validateVertex(v);  // Validate if the vertex index is valid
        return adj.get(v);
    }

    /**
     * Adds a directed edge from the 'from' node to the 'to' node with a specified capacity.
     * Also adds a reverse edge with zero capacity for the residual graph.
     *
     * @param from the starting vertex of the edge
     * @param to the ending vertex of the edge
     * @param capacity the capacity of the edge (must be positive)
     * @throws IllegalArgumentException if the capacity is not positive or vertices are invalid
     */
    public void addEdge(int from, int to, int capacity) {
        validateVertex(from);  // Validate the 'from' vertex
        validateVertex(to);    // Validate the 'to' vertex

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }

        // Create the forward edge (from -> to) with given capacity
        Edge e1 = new Edge(to, capacity);

        // Create the reverse edge (to -> from) with zero capacity (used for residual capacity)
        Edge e2 = new Edge(from, 0);

        // Set reverse edges to form the residual graph
        e1.reverse = e2;
        e2.reverse = e1;

        // Add the edges to the adjacency list
        adj.get(from).add(e1);
        adj.get(to).add(e2); // Add reverse edge to the destination vertex
    }

    /**
     * Helper method to validate that a given vertex index is within valid range.
     *
     * @param v the vertex index to validate
     * @throws IllegalArgumentException if the vertex index is out of bounds
     */
    private void validateVertex(int v) {
        if (v < 0 || v >= vertices) {
            throw new IllegalArgumentException("Invalid vertex: " + v);
        }
    }
}
