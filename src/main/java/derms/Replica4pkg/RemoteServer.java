//RemoteServer.java
package derms.Replica4pkg;

import javax.xml.ws.Endpoint;
import java.util.*;

public class RemoteServer {
    private static List<Endpoint> endpoints = new ArrayList<>();
    
        public static void main(String[] args) {
            try {
                Map<String, Integer> UDPPorts = new HashMap<>();
                UDPPorts.put("MTL", 4000);
                UDPPorts.put("QUE", 5000);
                UDPPorts.put("SHE", 6000);
    
                String[] serverNames = {"MTL", "QUE", "SHE"};
                int i = 0;
                for (String serverName : serverNames) {
                    int UDPPort = UDPPorts.get(serverName);
                    Replica4 server = new Replica4(null);
                    server.initReplica4(serverName, UDPPort, UDPPorts);
                    int port = 8080 + i;
                    String url = "http://localhost:" + port + "/DERMS/" + serverName;
                    Endpoint endpoint = Endpoint.publish(url, server);
                    endpoints.add(endpoint);  // Keep track of the Endpoint
                i++;
                System.out.println(serverName + " Server ready and waiting ...");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to stop all servers
    public void stopServers() {
        for (Endpoint endpoint : endpoints) {
            endpoint.stop();
        }
        System.out.println("All servers have been stopped.");
    }
}