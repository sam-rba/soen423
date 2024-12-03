package derms.replica1;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.Map;

@WebService
public interface CentralRepoInterface {
    @WebMethod
    Map<String, Integer> listServers();

    @WebMethod
    void addServer(String serverName, int serverPort);
}