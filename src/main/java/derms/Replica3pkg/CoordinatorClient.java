//CoordinatorCLient.java
package derms.Replica3pkg;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.io.*;
import java.util.*;

public class CoordinatorClient {
    private String coordinatorID;
    private String serverName;
    private DERMSInterface server;

    public CoordinatorClient(String coordinatorID) {
        this.coordinatorID = coordinatorID;
        this.serverName = coordinatorID.substring(0, 3).toUpperCase();
    }

    public void connectToServer() {
        try {
            String endpointURL = "";
            switch (serverName) {
                case "MTL":
                    endpointURL = "http://localhost:8080/DERMS/MTL?wsdl";
                    break;
                case "QUE":
                    endpointURL = "http://localhost:8081/DERMS/QUE?wsdl";
                    break;
                case "SHE":
                    endpointURL = "http://localhost:8082/DERMS/SHE?wsdl";
                    break;
                default:
                    System.out.println("Invalid server code in coordinatorID.");
            }

            URL url = new URL(endpointURL);
            QName qname = new QName("http://DERMS.org/", "ServerService");
            Service service = Service.create(url, qname);
            server = service.getPort(DERMSInterface.class);

            System.out.println("Connected to " + serverName + " server.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String requestResource(String resourceID, int duration) {
        try {
            String response = server.requestResource(coordinatorID, resourceID, duration);
            log("requestResource", response);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }

    public String findResource(String resourceName) {
        try {
            String response = server.findResource(coordinatorID, resourceName);
            log("findResource", response);
            System.out.println("Resources you have occupied:\n" + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }

    public String returnResource(String resourceID) {
        try {
            String response = server.returnResource(coordinatorID, resourceID);
            log("returnResource", response);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }
    
    public String swapResource(String coordinatorID, String oldResourceID, String oldResourceType, String newResourceID, String newResourceType) {
        try {
            String response = server.swapResource(coordinatorID, oldResourceID, oldResourceType, newResourceID, newResourceType);
            log("swapResource", response);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }

    private void log(String operation, String response) {
        try (FileWriter fw = new FileWriter(coordinatorID + "_log.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            String logEntry = String.format("%s - Operation: %s, Response: %s", new Date(), operation, response);
            out.println(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        connectToServer();

        while (true) {
            System.out.println("Coordinator menu:");
            System.out.println("1. Request Resource");
            System.out.println("2. Find Resource");
            System.out.println("3. Return Resource");
            System.out.println("4. Swap Resource");
            System.out.println("5. Exit");
            System.out.print("Enter choice (number): ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    System.out.print("Enter resourceID: ");
                    String resourceID = scanner.nextLine();
                    System.out.print("Enter duration: ");
                    int duration = Integer.parseInt(scanner.nextLine());
                    requestResource(resourceID, duration);
                    break;
                case 2:
                    System.out.print("Enter resourceName: ");
                    String resourceName = scanner.nextLine();
                    findResource(resourceName);
                    break;
                case 3:
                    System.out.print("Enter resourceID: ");
                    resourceID = scanner.nextLine();
                    returnResource(resourceID);
                    break;
                case 4:
                    swapResource();
                    break;    
                case 5:
                    System.out.println("Exiting client");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid input");
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter coordinatorID: ");
        String coordinatorID = scanner.nextLine();
        CoordinatorClient client = new CoordinatorClient(coordinatorID);
        client.run();
    }
}