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

}