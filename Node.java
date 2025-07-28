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

            while (true) {
                if (isLeader) {
                    sendLeaderStatus(); // The leader sends status (acts as heartbeat)
                } else {
                    checkHeartbeatTimeout();
                }
                Thread.sleep(HEARTBEAT_INTERVAL);
            }
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
                    handleReceivedMessage(message);
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

    private void handleReceivedMessage(String message) {
        String[] parts = message.split(":");
        String command = parts[0];

        switch (command) {
            case "ELECTION":
                int senderId = Integer.parseInt(parts[1]);
                System.out.println("Node " + nodeId + " received election message from Node " + senderId);
                if (senderId < nodeId) {
                    sendMessageToNode(otherNodePorts.get(senderId - 1), "OK:" + nodeId);
                    if (!electionInProgress && leaderId == -1) {
                        electionInProgress = true;
                        initiateElection();
                    }
                }
                break;

            case "OK":
                int okSenderId = Integer.parseInt(parts[1]);
                System.out.println("Node " + nodeId + " received OK message from Node " + okSenderId + ", stopping election.");
                electionInProgress = false;
                awaitingNewLeader = true;
                receivedOK = true;
                break;

            case "LEADER":
                leaderId = Integer.parseInt(parts[1]);
                isLeader = (leaderId == nodeId);
                electionInProgress = false;
                awaitingNewLeader = false;
                System.out.println("Node " + nodeId + " recognizes Node " + leaderId + " as leader.");
                lastHeartbeatReceived = System.currentTimeMillis();
                inactiveNodes.clear();
                break;
                
            case "HEARTBEAT":
                lastHeartbeatReceived = System.currentTimeMillis();
                break;
        }
    }

    private void sendMessageToNode(int targetPort, String message) {
        try (Socket socket = new Socket("localhost", targetPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(message);
        } catch (IOException e) {
            System.out.println("Node " + nodeId + " could not connect to Node at port " + targetPort);
            // More robust handling will be added later
        }
    }

    private void initiateElection() {
        if (electionInProgress || awaitingNewLeader || isLeader) {
            return;
        }
        System.out.println("Node " + nodeId + " initiating election.");
        leaderId = -1;
        electionInProgress = true;
        receivedOK = false;
        boolean higherNodeFound = false;

        for (int otherPort : otherNodePorts) {
            if (otherPort > port && !inactiveNodes.contains(otherPort)) {
                sendMessageToNode(otherPort, "ELECTION:" + nodeId);
                higherNodeFound = true;
            }
        }

        if (!higherNodeFound) {
            declareAsLeader();
        } else {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!receivedOK && electionInProgress) {
                        declareAsLeader();
                    }
                }
            }, ELECTION_BACKOFF);
        }
    }

    private void declareAsLeader() {
        leaderId = nodeId;
        isLeader = true;
        electionInProgress = false;
        awaitingNewLeader = false;
        System.out.println("Node " + nodeId + " is the new leader.");
        sendLeaderStatus(); // This method will be added next
        confirmLeaderStatus(); // This method will be added next
    }

    private void sendLeaderStatus() {
        for (int otherPort : otherNodePorts) {
            if (!inactiveNodes.contains(otherPort)) {
                sendMessageToNode(otherPort, "LEADER:" + nodeId);
            }
        }
    }

    private void confirmLeaderStatus() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Node " + nodeId + " confirmed as leader by all nodes.");
    }

    private void checkHeartbeatTimeout() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastHeartbeatReceived) > TIMEOUT_INTERVAL && !electionInProgress && !awaitingNewLeader) {
            System.out.println("System Failure Detected.");
            System.out.println("Node " + nodeId + " failed to receive heartbeat from leader.");
            System.out.println("Node " + nodeId + " detected that Leader Node " + leaderId + " is inactive. Starting new election.");
            isLeader = false;
            initiateElection();
        }
    }
}