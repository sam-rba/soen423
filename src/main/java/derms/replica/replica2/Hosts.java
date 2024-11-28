package derms.replica.replica2;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Hosts {
    private static Map<City, String> hosts = null;

    public static String get(City city) throws UnknownHostException {
        if (hosts == null)
            init();

        String host = hosts.get(city);
        if (host == null)
            throw new UnknownHostException("unknown host: "+city);
        return host;
    }

    private static void init() {
        hosts = new HashMap<City, String>();
        hosts.put(new City("MTL"), "alpine1");
        hosts.put(new City("QUE"), "alpine2");
        hosts.put(new City("SHE"), "alpine3");
    }
}
