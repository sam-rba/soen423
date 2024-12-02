package derms;
import derms.frontend.FEInterface;

import derms.Replica1;

import derms.Request;
import derms.Response;
import derms.net.MessagePayload;
import derms.net.tomulticast.TotalOrderMulticastReceiver;
import derms.net.runicast.ReliableUnicastSender;
import derms.net.tomulticast.TotalOrderMulticastSender;
import derms.replica2.Replica2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

public class ReplicaManager {
    private final int replicaId;
    private Replica replica;
    private Response response;
    private final Logger log;
    private InetSocketAddress frontEndAddress;
    private ReliableUnicastSender<Response> unicastSender;
    private TotalOrderMulticastReceiver multicastReceiver;

    public ReplicaManager(int replicaId) throws IOException {
        this.replicaId = replicaId;
        this.log = Logger.getLogger(getClass().getName());
        initUnicastSender();
        initReplica();
        initMulticastReceiver();
        startHeartbeatThread();
    }

    private void initUnicastSender() throws IOException {
        int frontEndPort = Config.frontendResponsePorts[replicaId - 1];
        frontEndAddress = new InetSocketAddress("localhost", frontEndPort);
        unicastSender = new ReliableUnicastSender<>(frontEndAddress);
    }

    private void initReplica() throws IOException {
        switch (replicaId) {
            case 1:
                replica = new derms.replica2.Replica2(new derms.replica2.City(), this);
                break;
            case 2:
                replica = new derms.replica2.Replica2(new derms.replica2.City(), this);
                break;
            case 3:
                replica = new derms.replica2.Replica2(new derms.replica2.City(), this);
                break;
            case 4:
                replica = new derms.replica2.Replica2(new derms.replica2.City(), this);
                break;
        }
        replica.startProcess();
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
                    informFrontEndRmIsDown(replica.getId());
                    replica.restart();
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
        replica.restart();
        informFrontEndRmHasBug(replica.getId());
    }

    private void informFrontEndRmIsDown(int replicaId) {
        try (Socket socket = new Socket(frontEndAddress.getAddress(), frontEndAddress.getPort());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.writeObject("RM_DOWN:" + replicaId);
        } catch (IOException e) {
            log.severe("Failed to inform FE that RM is down: " + e.getMessage());
        }
    }

    private void informFrontEndRmHasBug(int replicaId) {
        try (Socket socket = new Socket(frontEndAddress.getAddress(), frontEndAddress.getPort());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.writeObject("RM_BUG:" + replicaId);
        } catch (IOException e) {
            log.severe("Failed to inform FE that RM has a bug: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java ReplicaManager <replicaId>");
            System.exit(1);
        }

        int replicaId = Integer.parseInt(args[0]);

        try {
            ReplicaManager replicaManager = new ReplicaManager(replicaId);
            System.out.println("ReplicaManager " + replicaId + " is running.");
        } catch (IOException e) {
            System.err.println("Failed to start ReplicaManager: " + e.getMessage());
            e.printStackTrace();
        }
    }
}