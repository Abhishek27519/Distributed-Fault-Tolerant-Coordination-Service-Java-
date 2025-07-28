# Distributed-Fault-Tolerant-Coordination-Service-Java-

# Distributed Leader Election using the Bully Algorithm

A Java-based simulation of a fault-tolerant coordination service that uses the Bully Algorithm for dynamic leader election in a distributed system.

--

## 📖 About The Project

This project provides a hands-on implementation of the **Bully Algorithm**, a classic algorithm for electing a leader in a synchronous distributed system. The simulation creates a network of nodes that communicate over TCP/IP sockets to decide on a single coordinator or "leader."

The system is designed to be **fault-tolerant**. If the current leader node fails (or is manually stopped), the remaining nodes will detect its absence via a heartbeat mechanism and automatically initiate a new election to choose the next leader with the highest process ID.

---

## ✨ Key Features

* **Dynamic Leader Election**: Nodes elect the one with the highest ID as the leader.
* **Fault Tolerance**: Heartbeats are used to monitor the leader's status. If the leader fails, a new election is triggered automatically.
* **TCP/IP Communication**: Nodes communicate using simple, socket-based messaging for election, victory, and heartbeat signals.
* **Clear Console Logging**: Each node provides real-time status updates, making it easy to observe the election process and leader status.

---

## 🔧 How It Works

The core logic is based on the Bully Algorithm:

1.  When a node detects that the leader has failed, it starts an **election**.
2.  It sends an `ELECTION` message to all other nodes with a **higher process ID**.
3.  If it receives no `OK` response from any higher-ID nodes, it considers itself the winner, "bullies" the others, and declares itself the new **leader**.
4.  If it does receive an `OK` from a higher-ID node, it backs down, knowing that a more capable node will take over the election process.

---

## 🛠️ Technologies Used

* **Java (JDK 11+)**: The core programming language for the simulation logic.
* **Java Sockets**: For low-level TCP/IP network communication between nodes.
* **Shell Scripts**: Used to easily launch and configure each node instance in separate processes.

---

## 🚀 Getting Started

Follow these steps to get the simulation running on your local machine.

### Prerequisites

- Java Development Kit (JDK) version 11 or higher.
- Git for cloning the repository.

### Setup & Compilation

1.  **Clone the repository**:
    ```bash
    git clone [https://github.com/Abhishek27519/Distributed-Fault-Tolerant-Coordination-Service-Java-.git](https://github.com/Abhishek27519/Distributed-Fault-Tolerant-Coordination-Service-Java-.git)
    ```

2.  **Navigate to the project directory**:
    ```bash
    cd Distributed-Fault-Tolerant-Coordination-Service-Java-
    ```

3.  **Compile the Java source code: This command places the compiled `.class` files into the `target` directory**.
    ```bash
    javac -d target src/main/java/Node.java
    ```

---

## 🧪 Running the Simulation

To observe the system in action, you will need to open five separate terminal windows.

In each terminal, run one of the node scripts:

*  **Terminal 1**:
    ```bash
    ./scripts/nodes/node1/run_node1.sh
    ```
* **Terminal 2**:
    ```bash
    ./scripts/nodes/node2/run_node2.sh
    ```
* **Terminal 3**:
    ```bash
    ./scripts/nodes/node3/run_node3.sh
    ```
* **Terminal 4**:
    ```bash
    ./scripts/nodes/node4/run_node4.sh
    ```
* **Terminal 5**:
    ```bash
    ./scripts/nodes/node5/run_node5.sh
    ```

### Testing Fault Tolerance

1.  Initially, you will see the nodes start up and elect Node 5 as the leader (since it has the highest ID).
2.  To test the fault tolerance, go to the terminal running Node 5 and stop the process by pressing `Ctrl + C`.
3.  Observe the other terminals. After a short timeout, they will detect the leader's failure and start a new election.
4.  A new leader will be elected. Since Node 4 now has the highest active ID, it will become the new leader.
