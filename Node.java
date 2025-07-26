import java.io.*;
import java.net.*;
import java.util.*;

public class Node {
    private int nodeId;
    private int leaderId = -1;
    private List<Integer> otherNodePorts;
    private int port;
    private boolean isLeader = false;
    private ServerSocket serverSocket;
    private static final int HEARTBEAT_INTERVAL = 3000;
    private static final int TIMEOUT_INTERVAL = 10000;
    private static final int ELECTION_BACKOFF = 15000;
    private static final int INACTIVITY_THRESHOLD = 3;
    private long lastHeartbeatReceived;
    private boolean electionInProgress = false;
    private boolean awaitingNewLeader = false;
    private boolean receivedOK = false;

    private Map<Integer, Integer> retryCounts = new HashMap<>();
    private Set<Integer> inactiveNodes = new HashSet<>();

    public Node(int nodeId, int port, List<Integer> otherNodePorts) {
        this.nodeId = nodeId;
        this.port = port;
        this.otherNodePorts = otherNodePorts;
    }

    private void startNode() {
        try {
            Thread.sleep(2000 * nodeId); // Stagger startup
            serverSocket = new ServerSocket(port);
            lastHeartbeatReceived = System.currentTimeMillis();
            new Thread(this::listenForMessages).start();

            Thread.sleep(5000); // Wait for all nodes to start

            if (leaderId == -1) {
                initiateElection();
            }

        // Main loop will be added later
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForMessages() {
        while (true) {
            try (Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String message = in.readLine();
                if (message != null) {
                    // handleReceivedMessage(message); // Will be implemented next
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 5) {
            System.out.println("Usage: java Node <nodeId> <port> <otherNodePort1> <otherNodePort2> ...");
            return;
        }

        int nodeId = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);
        List<Integer> otherNodePorts = new ArrayList<>();

        for (int i = 2; i < args.length; i++) {
            otherNodePorts.add(Integer.parseInt(args[i]));
        }

        Node node = new Node(nodeId, port, otherNodePorts);
        node.startNode();
    }
}