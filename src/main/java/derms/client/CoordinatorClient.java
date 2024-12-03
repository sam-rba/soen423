package derms.client;

import derms.frontend.DERMSInterface;

import java.net.MalformedURLException;

public class CoordinatorClient {
    private final DERMSInterface server;
    private final String coordinatorID;

    public CoordinatorClient(String coordinatorID, String FEhost) throws MalformedURLException {
        this.server = Client.connectToServer(FEhost);
        this.coordinatorID = coordinatorID;
    }

    public String requestResource(String resourceID, int duration) {
        return server.requestResource(coordinatorID, resourceID, duration);
    }

    public String findResource(String resourceName) {
        return server.findResource(coordinatorID, resourceName);
    }

    public String returnResource(String resourceID) {
        return server.returnResource(coordinatorID, resourceID);
    }

    public String swapResource(String oldResourceID, String oldResourceType, String newResourceID, String newResourceType) {
        return server.swapResource(coordinatorID, oldResourceID, oldResourceType, newResourceID, newResourceType);
    }
}
