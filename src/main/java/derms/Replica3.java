package derms;

import derms.Replica3pkg.CoordinatorClient;
import derms.Replica3pkg.RemoteServer;
import derms.Replica3pkg.ResponderClient;
import derms.Replica3pkg.Server;
import derms.net.runicast.ReliableUnicastSender;
import derms.Replica;
import derms.ReplicaManager;
import derms.Request;
import derms.Response;
import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Replica3 implements Replica {

    private ReplicaManager replicaManager;
    private RemoteServer remoteServer;
    private boolean alive = true;
    
    public Replica3(ReplicaManager replicaManager){
        this.replicaManager = replicaManager;
        this.remoteServer = new RemoteServer();
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void startProcess() {
        
    }

    @Override
    public void processRequest(Request request) {
        ResponderClient responderClient;
        CoordinatorClient coordinatorClient;
        String responseMessage;
        switch (request.getFunction()) {
            case "addResource":
                responderClient = new ResponderClient(request.getClientID());
                responseMessage = responderClient.addResource(request.getResourceID(), request.getResourceType(), request.getDuration());
                break;
            case "removeResource":
                responderClient = new ResponderClient(request.getClientID());
                responseMessage = responderClient.removeResource(request.getResourceID(), request.getDuration());
                break;
            case "listResourceAvailability":
                responderClient = new ResponderClient(request.getClientID());
                responseMessage = responderClient.listResourceAvailability(request.getResourceType());
                break;
            case "requestResource":
                coordinatorClient = new CoordinatorClient(request.getClientID());
                responseMessage = coordinatorClient.requestResource(request.getResourceID(), request.getDuration());
                break;
            case "findResource":
                coordinatorClient = new CoordinatorClient(request.getClientID());
                responseMessage = coordinatorClient.findResource(request.getResourceType());
                break;
            case "returnResource":
                coordinatorClient = new CoordinatorClient(request.getClientID());
                responseMessage = coordinatorClient.returnResource(request.getResourceID());
                break;
            case "swapResource":
                coordinatorClient = new CoordinatorClient(request.getClientID());
                responseMessage = coordinatorClient.swapResource(
                        request.getClientID(),
                        request.getOldResourceID(),
                        request.getOldResourceType(),
                        request.getResourceID(),
                        request.getResourceType()
                );
                break;
            default:
                responseMessage = "Unrecognized function: " + request.getFunction();
                log.severe("Unrecognized function in request: " + request.getFunction());
                break;
        }

        Response response = new Response(request.getSequenceNumber(), responseMessage);
        log.info("Replica " + 3 + " processed request: " + request + ", response: " + response);
        replicaManager.sendResponseToFE(response);
    }

    @Override
    public void restart() {

    }

    @Override
    public int getId() {
        return 3;
    }
}