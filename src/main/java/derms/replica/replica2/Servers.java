package derms.replica.replica2;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Servers {
	private Map<City, InetAddress> servers = new ConcurrentHashMap<City, InetAddress>();

	/** Returns the address of the server located in the specified city, or null if there is no server in the city. */
	InetAddress get(City city) {
		return servers.get(city);
	}

	/**
	 *  Associates the specified server address with the specified city.
	 * If there was already a server associated with the city, the old value is replaced.
	 * @param city the city where the server is located
	 * @param addr the address of the server
	 * @return the previous server address, or null if there was no server associated with this city.
	 */
	InetAddress put(City city, InetAddress addr) {
		return servers.put(city, addr);
	}

	Collection<InetAddress> all() {
		return servers.values();
	}

	int size() {
		return servers.size();
	}
}
