//ResponderClient.java
package derms.Replica3pkg;


import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.*;
import java.net.URL;
import java.util.*;

public class ResponderClient {
    private String responderID;
    private String serverName;
    private Map<String, DERMSInterface> servers;

    public ResponderClient(String responderID) {
        this.responderID = responderID;
        this.serverName = responderID.substring(0, 3).toUpperCase();
        this.servers = new HashMap<>();
    }

    public void connectToServer() {
        try {
            String[] serverNames = {"MTL", "QUE", "SHE"};
            String[] endpoints = {
                    "http://localhost:8080/DERMS/MTL?wsdl",
                    "http://localhost:8081/DERMS/QUE?wsdl",
                    "http://localhost:8082/DERMS/SHE?wsdl"
            };

            int i = 0;
            for (String name : serverNames) {
                URL url = new URL(endpoints[i]);
                QName qname = new QName("http://DERMS.org/", "ServerService");
                Service service = Service.create(url, qname);
                servers.put(name, service.getPort(DERMSInterface.class));
                System.out.println("Connected to " + name + " server.");
                i++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public String addResource(String resourceID, String resourceName, int duration) {
            try {
                String serverCode = resourceID.substring(0, 3).toUpperCase();
                DERMSInterface targetServer = servers.get(serverCode);
                if (targetServer == null) {
                    System.out.println("Invalid server code in resourceID.");
                    return "Error";
                }
                String response = targetServer.addResource(resourceID, resourceName, duration);
                log("addResource", response);
                System.out.println(response);
                return "OK";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }
        }

        public String removeResource(String resourceID, int duration) {
            try {
                String serverCode = resourceID.substring(0, 3).toUpperCase();
                DERMSInterface targetServer = servers.get(serverCode);
                if (targetServer == null) {
                    System.out.println("Invalid server code in resourceID.");
                }
                String response = targetServer.removeResource(resourceID, duration);
                log("removeResource", response);
                System.out.println(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "OK";
        }

    public String listResourceAvailability(String resourceName) {
        try {
            String response = servers.get(serverName).listResourceAvailability(resourceName);
            log("listResourceAvailability", response);
            System.out.println("Available Resources:\n" + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }

    private void log(String operation, String response) {
        try (FileWriter fw = new FileWriter(responderID + "_log.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            String logEntry = String.format("%s - Operation: %s, Response: %s", new Date(), operation, response);
            out.println(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        connectToServer();

        while (true) {
            System.out.println("Responder menu:");
            System.out.println("1. Add Resource");
            System.out.println("2. Remove Resource");
            System.out.println("3. List Resource Availability");
            System.out.println("4. Exit");
            System.out.print("Enter choice (number): ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    System.out.print("Enter resourceID: ");
                    String resourceID = scanner.nextLine();
                    System.out.print("Enter resourceName: ");
                    String resourceName = scanner.nextLine();
                    System.out.print("Enter duration: ");
                    int duration = Integer.parseInt(scanner.nextLine());
                    addResource(resourceID, resourceName, duration);
                    break;
                case 2:
                    System.out.print("Enter resourceID: ");
                    resourceID = scanner.nextLine();
                    System.out.print("Enter duration (removal): ");
                    duration = Integer.parseInt(scanner.nextLine());
                    removeResource(resourceID, duration);
                    break;
                case 3:
                    System.out.print("Enter resourceName: ");
                    resourceName = scanner.nextLine();
                    listResourceAvailability(resourceName);
                    break;
                case 4:
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
        System.out.print("Enter responderID: ");
        String responderID = scanner.nextLine();
        ResponderClient client = new ResponderClient(responderID);
        client.start();
    }
}