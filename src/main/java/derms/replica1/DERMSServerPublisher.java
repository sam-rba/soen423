package derms.replica1;

import javax.xml.ws.Endpoint;

import derms.frontend.DERMSServerImpl;

public class DERMSServerPublisher {

    private static Endpoint endpoint;

    public static void main(String[] args) {
        // Publish the web service
        endpoint = Endpoint.publish("http://127.0.0.1:8387/ws/derms", new DERMSServerImpl());
        System.out.println("DERMS Server is published at http://127.0.0.1:8387/ws/derms");
    }

    public static void stop() {
        if (endpoint != null && endpoint.isPublished()) {
            endpoint.stop();
            System.out.println("DERMS Server is stopped.");
        }
    }
}