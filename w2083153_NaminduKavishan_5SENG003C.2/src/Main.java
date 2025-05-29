import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Student ID: 20230945
 *  Westminster ID: w2083153
 * Name: Namindu Kavishan
 * University of Westminster - 5SENG003C.2
 */
public class Main {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        File resultsDir = new File("test_results");
        if (!resultsDir.exists()) {
            if (!resultsDir.mkdir()) {
                System.err.println("[ERROR] Could not create test_results directory. Please ensure you have write permissions.");
                return;
            }
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            try {
                System.out.println("\n+----------------------------------+");
                System.out.println("| Network Flow Algorithm Main Menu |");
                System.out.println("+----------------------------------+");
                System.out.println("| 1. Start                         |");
                System.out.println("| 2. Exit                          |");
                System.out.println("+----------------------------------+");
                System.out.print("Enter your choice (1 or 2): ");

                String choice = scanner.nextLine().trim();
                System.out.println("[DEBUG] Main menu input: '" + choice + "'");

                if (choice.equals("1")) {
                    boolean continueTesting = true;
                    while (continueTesting) {
                        List<String> availableFiles = listTxtFiles();
                        if (availableFiles.isEmpty()) {
                            System.out.println("[ERROR] No .txt files found in the project directory.");
                            break;
                        }

                        System.out.println("\nNetwork Flow Algorithm Implementation");
                        System.out.println("====================================");
                        System.out.println("Available benchmark files: " + availableFiles);
                        System.out.println("Enter the name of a file to test (e.g., bridge_1.txt), 'all' to test bridge_1.txt to bridge_3.txt, or 'back' to return to main menu:");
                        String input = scanner.nextLine().trim();
                        System.out.println("[DEBUG] Raw input received: '" + input + "'");

                        if (input.isEmpty()) {
                            System.out.println("[ERROR] No file name entered.");
                            continue;
                        }

                        if (input.equalsIgnoreCase("back")) {
                            break;
                        }

                        List<String> filesToTest = new ArrayList<>();
                        if (input.equalsIgnoreCase("all")) {
                            for (String file : new String[]{"bridge_1.txt", "bridge_2.txt", "bridge_3.txt"}) {
                                if (availableFiles.contains(file)) {
                                    filesToTest.add(file);
                                }
                            }
                            if (filesToTest.isEmpty()) {
                                System.out.println("[ERROR] None of bridge_1.txt to bridge_3.txt were found.");
                                continue;
                            }
                        } else {
                            if (!availableFiles.contains(input)) {
                                System.out.println("[ERROR] File '" + input + "' not found.");
                                continue;
                            }
                            filesToTest.add(input);
                        }

                        for (String filename : filesToTest) {
                            System.out.println("\nProcessing file: " + filename);
                            PrintStream fileOut = null;
                            PrintStream dualOut = null;
                            try {
                                File inputFile = new File(filename);
                                if (inputFile.length() == 0) {
                                    throw new IllegalArgumentException("Input file '" + filename + "' is empty.");
                                }
                                if (inputFile.length() > 10 * 1024 * 1024) {
                                    throw new IllegalArgumentException("Input file '" + filename + "' is too large (exceeds 10 MB).");
                                }

                                String outputFilename = "test_results/" + filename.replace(".txt", "_output.txt");
                                fileOut = new PrintStream(new File(outputFilename));
                                dualOut = new PrintStream(new TeeOutputStream(System.out, fileOut));

                                String timestamp = LocalDateTime.now().format(formatter);
                                dualOut.println("--------------------------------------------------");
                                dualOut.println("Test run at: " + timestamp);
                                dualOut.println("Processing file: " + filename);
                                dualOut.println("--------------------------------------------------");

                                FlowNetwork network = parseNetwork(filename);
                                dualOut.println("Network loaded successfully!");

                                int numEdges = 0;
                                for (int v = 0; v < network.getVertices(); v++) {
                                    for (Edge e : network.getAdj(v)) {
                                        if (e.capacity > 0) numEdges++;
                                    }
                                }
                                numEdges /= 2;

                                dualOut.println("Network details:");
                                dualOut.println("---------------");
                                dualOut.println(network.getVertices() + " nodes, " + numEdges + " edges");

                                for (int v = 0; v < network.getVertices(); v++) {
                                    for (Edge e : network.getAdj(v)) {
                                        if (e.capacity > 0) {
                                            dualOut.println(v + "->" + e.to + " " + e.flow + "/" + e.capacity);
                                        }
                                    }
                                }
                                dualOut.println("");

                                if (numEdges == 0) {
                                    throw new IllegalArgumentException("Network has no edges.");
                                }

                                dualOut.println("Computing maximum flow from source node 0 to sink node " + (network.getVertices() - 1) + "...\n");
                                dualOut.println("Running Ford-Fulkerson algorithm (Edmonds-Karp implementation):");
                                dualOut.println("----------------------------------------------------------");

                                MaxFlowSolver solver = new MaxFlowSolver(network, 0, network.getVertices() - 1);
                                long startTime = System.nanoTime();
                                int maxFlow = solver.findMaxFlow(dualOut);
                                long endTime = System.nanoTime();
                                double executionTimeMs = (endTime - startTime) / 1_000_000.0;

                                dualOut.println("----------------------------------------------------------\n");
                                dualOut.println("========================");
                                dualOut.println("MAXIMUM FLOW RESULTS");
                                dualOut.println("========================");
                                dualOut.println("Maximum flow value: " + maxFlow);

                                dualOut.println("\nFinal flow assignment:");
                                dualOut.println("--------------------");

                                for (int v = 0; v < network.getVertices(); v++) {
                                    for (Edge e : network.getAdj(v)) {
                                        if (e.flow > 0) {
                                            dualOut.println("Edge " + v + "->" + e.to + " " + e.flow + "/" + e.capacity + " (flow/capacity)");
                                        }
                                    }
                                }

                                dualOut.println("\nAlgorithm performance:");
                                dualOut.println("--------------------");
                                dualOut.printf("Execution time: %.4f ms%n", executionTimeMs);
                                dualOut.println("Network size: " + network.getVertices() + " nodes, " + numEdges + " edges");
                                dualOut.println("--------------------------------------------------");
                                dualOut.println("Results saved to: " + outputFilename);
                                dualOut.println("--------------------------------------------------");

                            } catch (Exception e) {
                                System.err.println("[ERROR] " + e.getMessage());
                            } finally {
                                if (dualOut != null) dualOut.close();
                                if (fileOut != null) fileOut.close();
                            }

                            while (true) {
                                System.out.println("\nWhat would you like to do next? (1: Input another file, 2: Exit to Main Menu)");
                                String nextChoice = scanner.nextLine().trim();
                                if (nextChoice.equals("1")) {
                                    break;
                                } else if (nextChoice.equals("2")) {
                                    System.out.println("Exiting current session. Returning to main menu...");
                                    continueTesting = false;
                                    break;
                                } else {
                                    System.out.println("[ERROR] Invalid choice. Please enter '1' or '2'.");
                                }
                            }
                        }
                    }
                } else if (choice.equals("2")) {
                    System.out.println("Exiting program. Goodbye!");
                    running = false;
                } else {
                    System.out.println("[ERROR] Invalid choice. Please enter '1' or '2'.");
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Unexpected error: " + e.getMessage());
                System.out.println("Returning to main menu...");
            }
        }

        scanner.close();
        System.out.println("Program terminated.");
    }

    private static List<String> listTxtFiles() {
        List<String> txtFiles = new ArrayList<>();
        File dir = new File(".");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files != null) {
            for (File file : files) {
                txtFiles.add(file.getName());
            }
        }
        return txtFiles;
    }

    static FlowNetwork parseNetwork(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String firstLine = br.readLine();
            if (firstLine == null || firstLine.trim().isEmpty()) {
                throw new IllegalArgumentException("File is empty or first line is missing.");
            }

            int n = Integer.parseInt(firstLine.trim());
            FlowNetwork network = new FlowNetwork(n);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.trim().split("\\s+");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Invalid format: " + line);
                }

                int from = Integer.parseInt(parts[0]);
                int to = Integer.parseInt(parts[1]);
                int capacity = Integer.parseInt(parts[2]);
                network.addEdge(from, to, capacity);
            }
            return network;
        }
    }
}

//
class TeeOutputStream extends OutputStream {
    private final OutputStream consoleOut;
    private final OutputStream fileOut;

    public TeeOutputStream(OutputStream consoleOut, OutputStream fileOut) {
        this.consoleOut = consoleOut;
        this.fileOut = fileOut;
    }

    @Override
    public void write(int b) throws IOException {
        consoleOut.write(b);
        fileOut.write(b);
    }

    @Override
    public void flush() throws IOException {
        consoleOut.flush();
        fileOut.flush();
    }

    @Override
    public void close() throws IOException {
        fileOut.close(); // Only close the file, not the console
    }
}
