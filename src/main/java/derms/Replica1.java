package derms;

import derms.net.runicast.ReliableUnicastSender;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class Replica1 implements Replica {
    private final ReliableUnicastSender<Response> unicastSender;
    ReplicaManager replicaManager;
    private final Logger log;
    private boolean alive = true;

    public Replica1( InetSocketAddress frontEndAddress) throws IOException {
        this.unicastSender = new ReliableUnicastSender<Response>(frontEndAddress);
        this.log = Logger.getLogger(getClass().getName());
    }


    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void startProcess() {
        // Simulate the replica process starting.
        log.info("Replica " + 1 + " started.");
    }

    @Override
    public void processRequest(Request request) {
        // Process the request and create a response.
        endpoint1 = Endpoint.publish("http://localhost:8080/ws/derms", new DERMSServer("MTL"));
        endpoint2 = Endpoint.publish("http://localhost:8081/ws/derms", new DERMSServer("QUE"));
        endpoint3 = Endpoint.publish("http://localhost:8082/ws/derms", new DERMSServer("SHE"));


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
                responseMessage = coordinatorClient.requestResource(request.getClientID(), request.getResourceID(), request.getDuration());
                break;
            case "findResource":
                coordinatorClient = new CoordinatorClient(request.getClientID());
                responseMessage = coordinatorClient.findResource(request.getClientID(), request.getResourceType());
                break;
            case "returnResource":
                coordinatorClient = new CoordinatorClient(request.getClientID());
                responseMessage = coordinatorClient.returnResource(request.getClientID(), request.getResourceID());
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
        log.info("Replica " + 1 + " processed request: " + request + ", response: " + response);
        replicaManager.sendResponseToFE(response);
    }

    @Override
    public void restart() {
        // Restart the replica process.
        log.warning("Replica " + 1 + " is restarting...");
        startProcess();
    }

    @Override
    public int getId() { return 1; }
}