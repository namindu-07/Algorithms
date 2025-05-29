import java.io.PrintStream;
import java.util.*;

/**
 * Student ID: 20230945
 * Westminster ID: w2083153
 * Name: Namindu Kavishan
 * University of Westminster - 5SENG003C.2

 * This class solves the Maximum Flow problem using the Edmonds-Karp algorithm,
 * an implementation of Ford-Fulkerson with BFS.
 */
public class MaxFlowSolver {
    private FlowNetwork network;  // The flow network (graph with nodes and edges)
    private int source;           // The source node from where flow starts
    private int sink;             // The sink node where flow should end

    /**
     * Constructor to initialize the solver with a flow network, source node, and sink node.
     * It validates that source and sink nodes are valid, different, and there is a path between them.
     *
     * @param network The flow network
     * @param source The source node
     * @param sink The sink node
     */
    public MaxFlowSolver(FlowNetwork network, int source, int sink) {
        if (network == null) {
            throw new IllegalArgumentException("Network cannot be null");
        }
        this.network = network;
        validateVertex(source);  // Ensure the source node is valid
        validateVertex(sink);    // Ensure the sink node is valid
        if (source == sink) {
            throw new IllegalArgumentException("Source and sink must be different.");
        }
        this.source = source;
        this.sink = sink;

        // Check if there is a valid path from source to sink before proceeding
        if (!hasPathFromSourceToSink()) {
            throw new IllegalArgumentException("No path exists from source to sink in the network.");
        }
    }

    /**
     * This method checks if there exists a path from the source to the sink using BFS.
     * It ensures that the algorithm only runs if a path exists in the residual graph.
     *
     * @return true if there is a path from source to sink, otherwise false.
     */
    private boolean hasPathFromSourceToSink() {
        boolean[] visited = new boolean[network.getVertices()];  // Keeps track of visited nodes
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            // Explore all adjacent edges of node u
            for (Edge e : network.getAdj(u)) {
                if (e.capacity > 0 && !visited[e.to]) {  // If there is residual capacity and the node hasn't been visited
                    if (e.to == sink) return true;  // If we reach the sink node, return true
                    visited[e.to] = true;
                    queue.add(e.to);  // Add the node to the queue for further exploration
                }
            }
        }
        return false;  // No path found from source to sink
    }

    /**
     * Finds and returns the maximum flow from the source to the sink in the flow network.
     * This is the main method where the Edmonds-Karp algorithm (BFS version of Ford-Fulkerson) is applied.
     *
     * @param out The output stream to print the results
     * @return The maximum flow value
     */
    public int findMaxFlow(PrintStream out) {
        if (out == null) {
            throw new IllegalArgumentException("Output stream cannot be null");
        }

        int maxFlow = 0;  // Total maximum flow initialized to 0
        int iteration = 0;  // Counter to track iterations

        // Keep finding augmenting paths until no more are found
        while (true) {
            // Arrays to track paths and flow
            int[] parent = new int[network.getVertices()];
            Arrays.fill(parent, -1);  // Initialize parent array with -1 (indicating no parent)
            Edge[] pathEdges = new Edge[network.getVertices()];
            int[] bottleneck = new int[network.getVertices()];
            Arrays.fill(bottleneck, Integer.MAX_VALUE);  // Initially, the bottleneck (minimum capacity) is infinite

            Queue<Integer> queue = new LinkedList<>();
            queue.add(source);
            parent[source] = source;  // Source node's parent is itself (starting point)

            // BFS to find an augmenting path
            while (!queue.isEmpty() && parent[sink] == -1) {
                int u = queue.poll();
                // Explore all adjacent edges of node u
                for (Edge e : network.getAdj(u)) {
                    int v = e.to;  // Destination node of the edge
                    if (parent[v] == -1 && e.capacity - e.flow > 0) {  // If there is residual capacity and the node is not visited
                        parent[v] = u;  // Set the parent of node v to u
                        pathEdges[v] = e;  // Save the edge leading to v
                        bottleneck[v] = Math.min(bottleneck[u], e.capacity - e.flow);  // Update the bottleneck of the path
                        queue.add(v);  // Add v to the queue for further exploration
                    }
                }
            }

            if (parent[sink] == -1) break;  // No augmenting path found, exit the loop

            // Find the flow (bottleneck) that can be pushed through the found path
            int flow = bottleneck[sink];
            maxFlow += flow;  // Add the flow to the total maximum flow

            // Update flows along the path (in forward and reverse edges)
            int v = sink;
            List<Integer> path = new ArrayList<>();
            while (v != source) {
                path.add(v);  // Add the node to the path
                Edge e = pathEdges[v];  // Get the edge from the parent
                e.flow += flow;  // Increase flow along the forward edge
                e.reverse.flow -= flow;  // Decrease flow along the reverse edge
                v = parent[v];  // Move to the parent node
            }
            path.add(source);
            Collections.reverse(path);  // Reverse the path to print it from source to sink

            // Print the path and flow details for this iteration
            StringBuilder pathStr = new StringBuilder();
            for (int i = 0; i < path.size() - 1; i++) {
                pathStr.append(path.get(i)).append("->").append(path.get(i + 1));
                if (i < path.size() - 2) pathStr.append(", ");
            }
            out.println("Iteration " + (++iteration) + ": Path found: " + pathStr + " (Capacity: " + flow + ") => New flow: " + maxFlow);
        }

        // Print the final maximum flow value
        out.println("Maximum flow: " + maxFlow);

        // Print the flow assignments for each edge
        out.println("\nFinal edge flows:");
        for (int u = 0; u < network.getVertices(); u++) {
            for (Edge e : network.getAdj(u)) {
                if (e.flow > 0) {
                    out.println("Edge (" + u + " -> " + e.to + "): flow = " + e.flow + ", capacity = " + e.capacity);
                }
            }
        }

        return maxFlow;  // Return the maximum flow calculated
    }

    /**
     * Helper method to validate that the vertex index is valid (within bounds).
     *
     * @param v The vertex index to validate
     */
    private void validateVertex(int v) {
        if (v < 0 || v >= network.getVertices()) {
            throw new IllegalArgumentException("Invalid vertex index: " + v);
        }
    }
}
