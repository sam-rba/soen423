package derms;
import derms.frontend.FEInterface;

import derms.Replica1;
import derms.Replica2;
import derms.Replica3;
import derms.Replica4;

import derms.Request;
import derms.Response;
import derms.net.MessagePayload;
import derms.net.tomulticast.TotalOrderMulticastReceiver;
import derms.net.runicast.ReliableUnicastSender;
import derms.net.tomulticast.TotalOrderMulticastSender;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class ReplicaManager {
    private final int replicaId;
    private Replica replica;
    private Response response;
    private final FEInterface frontEnd;
    private final Logger log;
    private ReliableUnicastSender <Response>unicastSender;

    private TotalOrderMulticastReceiver multicastReceiver;

    public ReplicaManager(int replicaId, FEInterface frontEnd) throws IOException {
        this.replicaId = replicaId;
        this.frontEnd = frontEnd;
        this.log = Logger.getLogger(getClass().getName());
        initReplica();
        initMulticastReceiver();
        startHeartbeatThread();
    }

    private void initReplica() throws IOException {
        InetSocketAddress frontEndAddress = new InetSocketAddress("localhost", 1999);
        switch (replicaId)
        case 1:
        replica = new Replica1(frontEndAddress);
        break;
        case 2:
        replica = new Replica2();
        break;
        case 3:
        replica = new Replica3( frontEndAddress);
        break;
        case 4:
        replica = new Replica4(frontEndAddress);
        break;
        replica.startProcess();
    }



    private void initMulticastReceiver() throws IOException {
        InetSocketAddress group = new InetSocketAddress("230.0.0.0", 4446); // Example multicast group and port for receiving requests
        InetAddress localAddress = InetAddress.getLocalHost(); // Your local address
        multicastReceiver = new TotalOrderMulticastReceiver<Request>(group, localAddress);


        new Thread(() -> {
            while (true) {
                try {
                    MessagePayload receivedPayload = multicastReceiver.receive();
                    Request request = (Request) receivedPayload;
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
                    frontEnd.informRmIsDown(replica.getId());
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
        frontEnd.informRmHasBug(replica.getId());
    }



}