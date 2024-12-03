package derms.replica1;

import javax.xml.ws.Endpoint;

public class DERMSServerPublisher {

    private static Endpoint[] endpoints = new Endpoint[3];

    public static void main(String[] args) {
        try {
            endpoints[0] = Endpoint.publish("http://localhost:8387/ws/derms", new DERMSServer("MTL"));
            endpoints[1] = Endpoint.publish("http://localhost:8081/ws/derms", new DERMSServer("QUE"));
            endpoints[3] = Endpoint.publish("http://localhost:8082/ws/derms", new DERMSServer("SHE"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop() {
        for (Endpoint endpoint : endpoints) {
            if (endpoint != null && endpoint.isPublished()) {
                endpoint.stop();
                System.out.println("DERMS Server is stopped.");
            }
        }
    }
}