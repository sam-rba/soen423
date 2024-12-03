package derms.replica1;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.Endpoint;

@WebService
public class CentralRepo {

    public static void main(String[] args) {
        Endpoint.publish("http://localhost:8387/CentralRepo", new CentralRepo());
        System.out.println("derms.CentralRepo is up and running...");
    }

    private final Map<String, Integer> serverNames = new HashMap<>();

    @WebMethod
    public Map<String, Integer> listServers() {
        return serverNames;
    }

    @WebMethod
    public void addServer(String serverName, int serverPort) {
        serverNames.put(serverName, serverPort);
    }
}