package derms.replica1;

import derms.Replica;
import derms.ReplicaManager;
import derms.Request;
import derms.Response;
import derms.replica2.DermsLogger;
import derms.util.TestLogger;
import derms.util.ThreadPool;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Replica1 implements Replica {

    private boolean alive = false;
    private final ExecutorService pool;
    private final Logger log;
    private final InetAddress localAddr;
//    private final ResponderClient responderClient;
//    private final CoordinatorClient coordinatorClient;
    private final String responderClientID = "MTL";
    private final String coordinatorClientID = "MTLC1111";
    private final ReplicaManager replicaManager;
    private DERMSServer server;
    private final ConcurrentHashMap<String, Integer> portsMap;
    private final Random r = new Random();

    private boolean byzFailure;

    public Replica1(ReplicaManager replicaManager) {
        this.replicaManager = replicaManager;
        pool = Executors.newFixedThreadPool(5);
        try {
            localAddr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
//        responderClient = new ResponderClient(responderClientID);
//        coordinatorClient = new CoordinatorClient(coordinatorClientID);
        try {
            this.log = DermsLogger.getLogger(getClass());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        portsMap = new ConcurrentHashMap<>();
        portsMap.put("MTL", r.nextInt(60000-8000) + 8000);
        portsMap.put("QUE", r.nextInt(60000-8000) + 8000);
        portsMap.put("SHE", r.nextInt(60000-8000) + 8000);
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void startProcess(int byzantine, int crash) {
         // [TEST] Detect crash
         if (crash == 1) {
            alive = false;
        } else {
            alive = true;
        }

        // [TEST] Detect byzantine failure
        if (byzantine == 1) {
            byzFailure = true;
        } else {
            byzFailure = false;
        }

        try {
            server = new DERMSServer("MTL", portsMap);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            new DERMSServer("SHE", portsMap);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            new DERMSServer("QUE", portsMap);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        try {
//            Endpoint.publish("http://localhost:8387/ws/derms", new DERMSServer("MTL"));
//            Endpoint.publish("http://localhost:8081/ws/derms", new DERMSServer("QUE"));
//            Endpoint.publish("http://localhost:8082/ws/derms", new DERMSServer("SHE"));
//        } catch (InterruptedException e) {}

        alive = true;
        log.info(getClass().getSimpleName() + " started.");
        log.config("Local address is "+localAddr.toString());
    }

    @Override
    public void processRequest(Request request) {
        // [TEST] Simulate byzantine failure (return incorrect value)
        if (byzFailure == true) {
            Response response = new Response(request, replicaManager.getReplicaId(), "BYZANTINE FAILURE", false);
            replicaManager.sendResponseToFE(response);
            return;
        }

        log.info(request.toString());

        String status = "";
        boolean isSuccess = true;
        try {
            switch (request.getFunction()) {
                case "addResource":
                    status = server.addResource(request.getResourceID(), request.getResourceType(), request.getDuration());
                    break;
                case "removeResource":
                    status = server.removeResource(request.getResourceID(), request.getDuration());
                    break;
                case "listResourceAvailability":
//                    status = String.join(",", responderClient.listResourceAvailability(request.getResourceType()));
                   status = String.join(",", server.listResourceAvailability(request.getResourceType()));
                    break;
                case "requestResource":
                    status = server.requestResource(coordinatorClientID, request.getResourceID(), request.getDuration());
                    break;
                case "findResource":
                    status = String.join(",", server.findResource(coordinatorClientID, request.getResourceType()));
                    break;
                case "returnResource":
                    status = server.returnResource(coordinatorClientID, request.getResourceID());
                    break;
                case "swapResource":
                    status = server.swapResource(coordinatorClientID, request.getOldResourceID(), request.getOldResourceType(), request.getResourceID(), request.getResourceType());
                    break;
                default:
                    status = "Failure: unknown function '" + request.getFunction() + "'";
            }
        } catch (Exception e) {
            log.warning(e.getMessage());
            status = "Failure: " + request.getFunction() + ": " + e.getMessage();
            isSuccess = false;
        }

        Response response = new Response(request, replicaManager.getReplicaId(), status, isSuccess); // TODO: isSuccess flag
        log.info("Processed request " + request + "; response: " + response);
        replicaManager.sendResponseToFE(response);
    }

    @Override
    public void restart() {
        log.info("Shutting down...");
        ThreadPool.shutdown(pool, log);
        alive = false;
        log.info("Finished shutting down.");

        // [TEST] Restart process without byzantine failure or crash
        TestLogger.log("REPLICA 1: {RESTARTED}");
        startProcess(0, 0);
    }

    @Override
    public int getId() {
        return 1;
    }
}
