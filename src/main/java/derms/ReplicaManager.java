package derms;
import derms.frontend.FEInterface;


import derms.Request;
import derms.Response;
import derms.net.MessagePayload;
import derms.net.tomulticast.TotalOrderMulticastReceiver;
import derms.net.runicast.ReliableUnicastSender;
import derms.net.tomulticast.TotalOrderMulticastSender;
import derms.replica1.Replica1;
import derms.replica2.Replica2;
import derms.util.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

public class ReplicaManager {
    public static final String usage = "Usage: java ReplicaManager <replicaId> <city> <frontEndIP> <byzantine(0 or 1)> <crash(0 or 1)>";
    private final int replicaId;
    private final String city;
    private Replica replica;
    private Response response;
    private final Logger log;
    private InetSocketAddress frontEndAddress;
    private ReliableUnicastSender<Response> unicastSender;
    private TotalOrderMulticastReceiver multicastReceiver;

    public ReplicaManager(int replicaId, String city, InetAddress frontEndIP, int byzantine, int crash) throws IOException {
        this.replicaId = replicaId;
        this.city = city;
        this.log = Logger.getLogger(getClass().getName());
        initUnicastSender(frontEndIP);
        initReplica(byzantine, crash);
        initMulticastReceiver();
        startHeartbeatThread();
    }

    private void initUnicastSender(InetAddress frontEndIP) throws IOException {
        int frontEndPort = Config.frontendResponsePorts[replicaId - 1];
        frontEndAddress = new InetSocketAddress(frontEndIP, frontEndPort);
        unicastSender = new ReliableUnicastSender<>(frontEndAddress);
    }

    private void initReplica(int byzantine, int crash) throws IOException {
        switch (replicaId) {
            case 1:
                replica = new Replica1(this);
                break;
            case 2:
                replica = new derms.replica2.Replica2(city, this);
                break;
            case 3:
                replica = new derms.replica3.Replica3(city, this);
                break;
            case 4:
                replica = new derms.replica2.Replica2(city, this);
                break;
        }

        // [TEST] Logging
        if (byzantine == 0) {
            TestLogger.log("REPLICA " + replicaId + ": {BYZANTINE: FALSE}");
        } else {
            TestLogger.log("REPLICA " + replicaId + ": {BYZANTINE: TRUE}");
        }

        if (crash == 0) {
            TestLogger.log("REPLICA " + replicaId + ": {CRASH: FALSE}");
        } else {
            TestLogger.log("REPLICA " + replicaId + ": {CRASH: TRUE}");
        }

        replica.startProcess(byzantine, crash);
    }

    private void initMulticastReceiver() throws IOException {
        InetSocketAddress group = Config.group;
        InetAddress localAddress = InetAddress.getLocalHost(); // Your local address
        NetworkInterface netInterface = NetworkInterface.getByInetAddress(localAddress);
        multicastReceiver = new TotalOrderMulticastReceiver<Request>(group, localAddress, netInterface);

        new Thread(() -> {
            while (true) {
                try {
                    MessagePayload receivedPayload = multicastReceiver.receive();
                    Request request = (Request) receivedPayload;
                    log.info("Received request: " + request);
                    replica.processRequest(request);
                } catch (InterruptedException e) {
                    log.severe("Failed to receive request: " + e.getMessage());
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.severe("Error processing request: " + e.getMessage());
                }
            }
        }).start();
    }

    private void startHeartbeatThread() {
        new Thread(() -> {
            while (true) {
                if (!replica.isAlive()) {
                    // [TEST] Logging
                    TestLogger.log("REPLICA " + replicaId + ": {CRASH: DETECTED}");

                    informFrontEndRmIsDown(replica.getId());
                    replica.restart();
                    //TestLogger.log("REPLICA " + replicaId + ": {RESTARTED}");
                }
                try {
                    Thread.sleep(5000); // Example 5 seconds.
                } catch (InterruptedException e) {
                    log.severe("Heartbeat thread interrupted: " + e.getMessage());
                }
            }
        }).start();
    }

    public void sendResponseToFE(Response response) {
        try {
            unicastSender.send(response);
        } catch (IOException e) {
            log.severe("Failed to send response to FE: " + e.getMessage());
        }
    }

    public void handleByzantineFailure() {
        log.severe("Byzantine failure detected in Replica " + replica.getId());

        // [TEST] Logging
        TestLogger.log("REPLICA " + replicaId + ": {BYZANTINE: DETECTED}");

        replica.restart();
        informFrontEndRmHasBug(replica.getId());
        //TestLogger.log("REPLICA " + replicaId + ": {RESTARTED}");
    }

    private void informFrontEndRmIsDown(int replicaId) {
        try (Socket socket = new Socket(frontEndAddress.getAddress(), frontEndAddress.getPort());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.writeObject("RM_DOWN:" + replicaId);
        } catch (IOException e) {
            log.severe("Failed to inform FE that RM is down: " + e.getMessage());
            TestLogger.log("[FAILED TO INFORM FE (RM IS DOWN)]");
        }
    }

    private void informFrontEndRmHasBug(int replicaId) {
        try (Socket socket = new Socket(frontEndAddress.getAddress(), frontEndAddress.getPort());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.writeObject("RM_BUG:" + replicaId);
        } catch (IOException e) {
            log.severe("Failed to inform FE that RM has a bug: " + e.getMessage());
            TestLogger.log("[FAILED TO INFORM FE (RM HAS A BUG)]");
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println(usage);
            System.exit(1);
        }

        try {
            int replicaId = Integer.parseInt(args[0]);
            String city = args[1];
            InetAddress frontEndIP = InetAddress.getByName(args[2]);
            int byzantine = Integer.parseInt(args[3]);
            int crash = Integer.parseInt(args[4]);
            ReplicaManager replicaManager = new ReplicaManager(replicaId, city, frontEndIP, byzantine, crash);
            System.out.println("ReplicaManager " + replicaId + " is running.");
        } catch (IOException e) {
            System.err.println("Failed to start ReplicaManager: " + e.getMessage());
            TestLogger.log("[FAILED TO START RM]");
            e.printStackTrace();
        }
    }

    public int getReplicaId() { return replicaId; }
}