package derms.replica1;

import derms.replica1.jaxws.DERMSInterface;
import derms.replica1.jaxws.DERMSServerService;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class CoordinatorClient {
    private final DERMSInterface server;
    private String coordinatorID;

    public CoordinatorClient(String coordinatorID) {
        try {
            this.coordinatorID = coordinatorID;
        HashMap<String, String> servers = new HashMap<String, String>() {{
                put("MTL", "http://localhost:8387/ws/derms?wsdl");
                put("QUE", "http://localhost:8081/ws/derms?wsdl");
                put("SHE", "http://localhost:8082/ws/derms?wsdl");
            }};
            URL url = new URL(servers.get(coordinatorID.substring(0, 3)));
            DERMSServerService service = new DERMSServerService(url);
            server = service.getDERMSServerPort();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String requestResource(String coordinatorID, String resourceID, int duration) {
        try {
            String response = server.requestResource(coordinatorID, resourceID, duration);
            logOperation("requestResource", coordinatorID, resourceID, duration, response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> findResource(String coordinatorID, String resourceName) {
        try {
            List<String> response = server.findResource(coordinatorID, resourceName);
            logOperation("findResource", coordinatorID, resourceName, response.toString());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public String returnResource(String coordinatorID, String resourceID) {
        try {
            String response = server.returnResource(coordinatorID, resourceID);
            System.out.println(response);
            logOperation("returnResource", coordinatorID, resourceID, response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public String swapResource(String coordinatorID, String oldResourceID, String oldResourceType, String newResourceID, String newResourceType) {
        try {
            String response = server.swapResource(coordinatorID, oldResourceID, oldResourceType, newResourceID, newResourceType);
            logOperation("swapResource", coordinatorID, oldResourceID, newResourceID, response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void logOperation(String operation, String coordinatorID, String oldResourceID, String newResourceID, String response) {
        try (FileWriter writer = new FileWriter(coordinatorID + "_log.txt", true)) {
            writer.write(LocalDateTime.now() + " - " + operation + " - " + coordinatorID + " - " + oldResourceID + " - " + newResourceID + " - " + response + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void logOperation(String operation, String coordinatorID, String resourceID, int duration, String response) {
        try (FileWriter writer = new FileWriter(coordinatorID + "_log.txt", true)) {
            writer.write(LocalDateTime.now() + " - " + operation + " - " + coordinatorID + " - " + resourceID + " - " + duration + " - " + response + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logOperation(String operation, String coordinatorID, String resourceName, String response) {
        try (FileWriter writer = new FileWriter(coordinatorID + "_log.txt", true)) {
            writer.write(LocalDateTime.now() + " - " + operation + " - " + coordinatorID + " - " + resourceName + " - " + response + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}