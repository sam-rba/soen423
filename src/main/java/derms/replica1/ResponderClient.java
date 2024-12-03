package derms.replica1;

import derms.replica1.jaxws.DERMSInterface;
import derms.replica1.jaxws.DERMSServerService;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public class ResponderClient {

    DERMSInterface server;
    private String responderID;

    public ResponderClient(String responderID) {
        try {
            this.responderID = responderID;
            HashMap<String, String> servers = new HashMap<String, String>() {{
                put("MTL", "http://localhost:8387/ws/derms?wsdl");
                put("QUE", "http://localhost:8081/ws/derms?wsdl");
                put("SHE", "http://localhost:8082/ws/derms?wsdl");
            }};
            URL url = new URL(servers.get(responderID.substring(0, 3)));
            DERMSServerService service = new DERMSServerService(url);
            server = service.getDERMSServerPort();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void respond() {
        try {
            String response = server.removeResource("MTL0012", 5);
            System.out.println("Response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ResponderClient client = new ResponderClient("Responder1");
        client.respond();
    }

    public String addResource(String resourceID, String resourceName, int duration) {
        try {
            String response = server.addResource( resourceID, resourceName, duration);
            System.out.println(response);
            logOperation("addResource", resourceID, resourceName, duration, response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String removeResource(String resourceID, int duration) {
        try {
            String response = server.removeResource(resourceID, duration);
            System.out.println(response);
            logOperation("removeResource", resourceID, duration, response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<String> listResourceAvailability(String resourceName) {
        try {
            List<String> response = server.listResourceAvailability(resourceName);
            logOperation("listResourceAvailability", resourceName, response.toString());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private void logOperation(String operation, String resourceID, String resourceName, int duration, String response) {
        try (FileWriter writer = new FileWriter(responderID + "_log.txt", true)) {
            writer.write(LocalDateTime.now() + " - " + operation + " - " + resourceID + " - " + resourceName + " - " + duration + " - " + response + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logOperation(String operation, String resourceID, int duration, String response) {
        try (FileWriter writer = new FileWriter(responderID + "_log.txt", true)) {
            writer.write(LocalDateTime.now() + " - " + operation + " - " + resourceID + " - " + duration + " - " + response + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logOperation(String operation, String resourceName, String response) {
        try (FileWriter writer = new FileWriter(responderID + "_log.txt", true)) {
            writer.write(LocalDateTime.now() + " - " + operation + " - " + resourceName + " - " + response + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}