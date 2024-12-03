package derms.replica1;

import derms.Replica;
import derms.ReplicaManager;
import derms.Request;
import derms.Response;
import derms.replica2.DermsLogger;
import derms.util.ThreadPool;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Replica1 implements Replica {

    private boolean alive = false;
    private final ExecutorService pool;
    private final Logger log;
    private final InetAddress localAddr;
    private final ResponderClient responderClient;
    private final ReplicaManager replicaManager;

    public Replica1(ReplicaManager replicaManager) {
        this.replicaManager = replicaManager;
        pool = Executors.newFixedThreadPool(5);
        try {
            localAddr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        responderClient = new ResponderClient("MTL");
        try {
            this.log = DermsLogger.getLogger(getClass());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void startProcess() {
        pool.execute(DERMSServer::new);
        alive = true;
        log.info(getClass().getSimpleName() + " started.");
        log.config("Local address is "+localAddr.toString());
    }

    @Override
    public void processRequest(Request request) {
        String status = responderClient.addResource(
                request.getResourceID(),
                request.getResourceType(),
                request.getDuration()
        );
        Response response = new Response(request.getSequenceNumber(), status);
        log.info("Processed request " + request + "; response: " + response);
        replicaManager.sendResponseToFE(response);
    }

    @Override
    public void restart() {
        log.info("Shutting down...");
        ThreadPool.shutdown(pool, log);
        alive = false;
        log.info("Finished shutting down.");
        startProcess();
    }

    @Override
    public int getId() {
        return 1;
    }
}
