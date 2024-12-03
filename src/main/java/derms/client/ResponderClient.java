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
        String res = server.addResource(resourceID, resourceName, duration);
        if (res.contains("Fail")) {
            TestLogger.log("[FAILED: " + res + "]");
        } else {
            TestLogger.log("[SUCCESS: " + res + "]");
        }
        return res;
    }

    public String removeResource(String resourceID, int duration) {
        return server.removeResource(resourceID, duration);
    }

    public String listResourceAvailability(String resourceName) {
        return server.listResourceAvailability(resourceName);
    }
}
