package derms.replica.replica2;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ResponderServer {
	public static final Duration timeout = Duration.ofSeconds(5);

	private City city;
	private Resources resources;
	private Servers servers;
	private Logger log;

	public ResponderServer(City city, Resources resources, Servers servers) throws IOException {
		this.city = city;
		this.resources = resources;
		this.servers = servers;
		this.log = DermsLogger.getLogger(this.getClass());
	}

	public ResponderServer() throws IOException {
		this(new City(), new Resources(), new Servers());
	}

	public void addResource(Resource r) {
		resources.add(r);
		log.info("Added resource "+r+" - success");
	}

	public void removeResource(ResourceID rid, int duration) throws NoSuchResourceException {
		log.info("Remove duration "+duration+" from "+rid);
		try {
			Resource resource = resources.getByID(rid);
			synchronized (resource) {
				if (duration < 0 || duration >= resource.duration) {
					resources.removeByID(rid);
					log.info("Removed resource "+rid+" - success");
				} else {
					resource.duration -= duration;
					log.info("Removed duration from resource "+rid+". New duration: "+resource.duration+" - success");
				}
			}
		} catch (NoSuchElementException e) {
			// Wanted to remove duration from resource.
			if (duration >= 0) {
				String msg = "Cannot remove duration from "+rid+": resource does not exist.";
				log.warning(msg);
				throw new NoSuchResourceException(msg);
			}

			// Duration is negative---wanted to remove resource completely.
			// Success because it is already removed.
			log.info("Not removing "+rid+": resource does not exist.");
		}
	}

	public Resource[] listResourceAvailability(ResourceName rname) throws ServerCommunicationError {
		log.info("Request for available "+rname);
		Collection<Resource> availableResources = ConcurrentHashMap.newKeySet();
		ExecutorService pool = Executors.newFixedThreadPool(servers.size());
		try {
			for (InetAddress serverAddr : servers.all()) {
				pool.execute(new ResourceAvailability.Client(serverAddr, rname, availableResources));
			}
		} catch (IOException e) {
			String msg = "Failed to start ResourceAvailability Client: "+e.getMessage();
			log.severe(msg);
			throw new ServerCommunicationError("ResourceAvailability: "+msg);
		}

		log.fine("Started worker threads");
		pool.shutdown();
		boolean terminated;
		try {
			terminated = pool.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new ServerCommunicationError("listResourceAvailability(): listResourceAvailability() interrupted: "+e.getMessage());
		}
		if (!terminated) {
			throw new ServerCommunicationError("ResourceAvailability: request timed out: no response after "+timeout.toString());
		}
		log.info("Response length "+availableResources.size());
		Resource[] arr = new Resource[0];
		return availableResources.toArray(arr);
	}
}
