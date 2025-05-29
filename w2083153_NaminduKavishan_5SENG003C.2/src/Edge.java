

/**
 * Student ID: 20230945
 *  Westminster ID: w2083153
 * Name: Namindu Kavishan
 * University of Westminster - 5SENG003C.2
 */
public class Edge {
    public int to;           // Destination node
    public int capacity;     // Maximum capacity of the edge
    public int flow;         // Current flow through the edge
    public Edge reverse;     // Reverse edge (used for residual graph)

    // Constructor to create an edge with a given destination and capacity
    public Edge(int to, int capacity) {
        this.to = to;
        this.capacity = capacity;
        this.flow = 0;
        this.reverse = null; // Will be linked later when adding edges
    }
}
