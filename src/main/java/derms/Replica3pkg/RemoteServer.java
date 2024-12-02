//RemoteServer.java
package derms.Replica3pkg;

import javax.xml.ws.Endpoint;
import java.util.*;

public class RemoteServer {
    public RemoteServer() {
        try {
            Map<String, Integer> UDPPorts = new HashMap<>();
            UDPPorts.put("MTL", 4000);
            UDPPorts.put("QUE", 5000);
            UDPPorts.put("SHE", 6000);

            String[] serverNames = {"MTL", "QUE", "SHE"};
            int i = 0;
            for (String serverName : serverNames) {
                int UDPPort = UDPPorts.get(serverName);
                Server server = new Server();
                server.initServer(serverName, UDPPort, UDPPorts);
                int port = 8080 + i;
                String url = "http://localhost:" + port + "/DERMS/" + serverName;
                Endpoint.publish(url, server);
                i++;
                System.out.println(serverName + " Server ready and waiting ...");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}