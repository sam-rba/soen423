package derms.client;

import derms.frontend.DERMSInterface;
import derms.util.TestLogger;

import java.net.MalformedURLException;
import java.util.Objects;

public class ResponderClient {
    private final DERMSInterface server;

    public ResponderClient(String FEhost) throws MalformedURLException {
        server = Client.connectToServer(FEhost);
    }

    public String addResource(String resourceID, String resourceName, int duration) {
        return server.addResource(resourceID, resourceName, duration);
    }

    public String removeResource(String resourceID, int duration) {
        return server.removeResource(resourceID, duration);
    }

    public String listResourceAvailability(String resourceName) {
        return server.listResourceAvailability(resourceName);
    }
}
