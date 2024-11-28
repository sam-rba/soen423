package derms.client;

import java.net.MalformedURLException;

public class ResponderClient extends Client {
    public static final String usage = "Usage: java derms.client.ResponderClienet <FE host>";

    public ResponderClient(String FEhost) throws MalformedURLException {
        super(FEhost);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Missing argument 'FE host'");
            System.exit(1);
        }

        String FEhost = args[0];

        ResponderClient client = null;
        try {
            client = new ResponderClient(FEhost);
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        System.out.println("Adding resource...");
        String response = client.addResource("MTL1234", "AMBULANCE", 100);
        System.out.println("Response: " + response);
    }

    public String addResource(String resourceID, String resourceName, int duration) {
        return server.addResource(resourceID, resourceName, duration);
    }
}
