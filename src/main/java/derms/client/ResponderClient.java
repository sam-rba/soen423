package derms.client;

import java.net.MalformedURLException;

class ResponderClient extends Client {
    ResponderClient(String FEhost) throws MalformedURLException {
        super(FEhost);
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
