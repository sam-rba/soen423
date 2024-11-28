package derms.replica.replica2;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

class CoordinatorServer {
	static final Duration timeout = Duration.ofSeconds(5);

	private City city;
	private Resources resources;
	private Servers servers;
	private Logger log;

	CoordinatorServer(City city, Resources resources, Servers servers) throws IOException {
		super();
		this.city = city;
		this.resources = resources;
		this.servers = servers;
		this.log = DermsLogger.getLogger(this.getClass());
	}

	CoordinatorServer() throws IOException {
		this(new City(), new Resources(), new Servers());
	}

	void requestResource(CoordinatorID cid, ResourceID rid, int duration)
			throws ServerCommunicationError, NoSuchResourceException,
			AlreadyBorrowedException, InvalidDurationException
	{
		log.info("Request for "+rid+" from "+cid);

		InetAddress server = servers.get(new City(rid.city));
		if (server == null) {
			throw new ServerCommunicationError("requestResource(): no connection to server "+rid.city.toString());
		}

		RequestResource.Client client = new RequestResource.Client(cid, rid, duration);
		RequestResource.Response response;
		try {
			response = client.sendRequest(server);
		} catch (IOException e) {
			throw new ServerCommunicationError("requestResource(): "+e.getMessage());
		}
		switch (response.status) {
		case SUCCESS:
			log.info("Request "+rid+" from "+cid+" - success");
			break;
		case NO_SUCH_RESOURCE:
			log.warning(response.message);
			throw new NoSuchResourceException(response.message);
		case ALREADY_BORROWED:
			log.warning(response.message);
			throw new AlreadyBorrowedException(response.message);
		case INVALID_DURATION:
			log.warning(response.message);
			throw new InvalidDurationException(response.message);
		default:
			log.warning("Unsuccessful response from server: "+response.message);
			throw new ServerCommunicationError("requestResource(): failed to borrow resource: "+response.message);
		}
	}

	Resource[] findResource(CoordinatorID cid, ResourceType rname) throws ServerCommunicationError {
		log.info("Find Resource "+rname+" from "+cid);
		FindResource.Request request = new FindResource.Request(cid, rname);
		Collection<Resource> response = ConcurrentHashMap.newKeySet();
		ExecutorService pool = Executors.newFixedThreadPool(servers.size());
		try {
			for (InetAddress server : servers.all()) {
				pool.execute(new FindResource.Client(request, server, response));
			}
		} catch (IOException e) {
			String msg = "Failed to start FindResource Client: "+e.getMessage();
			log.severe(msg);
			throw new ServerCommunicationError("findResource(): "+msg);
		}
		log.fine("Started worker threads");
		pool.shutdown();
		boolean terminated;
		try {
			terminated  = pool.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			String msg = "findResource() interrupted: "+e.getMessage();
			log.warning(msg);
			throw new ServerCommunicationError("findResource(): "+msg);
		}
		if (!terminated) {
			String msg = "Request timed out: no response after "+timeout.toString();
			log.warning(msg);
			throw new ServerCommunicationError("findResource(): "+msg);
		}
		Resource[] arr = new Resource[0];
		arr = response.toArray(arr);
		log.info("Find resource "+rname+" from "+cid+" - success. Response length: "+arr.length);
		return arr;
	}

	void returnResource(CoordinatorID cid, ResourceID rid)
			throws ServerCommunicationError, NoSuchResourceException, NotBorrowedException
	{
		log.info("Return resource "+rid+" from "+cid);
		InetAddress server = servers.get(new City(rid.city));
		if (server == null) {
			String msg = "no connection to server "+rid.city;
			log.warning(msg);
			throw new ServerCommunicationError("returnResource(): "+msg);
		}
		log.fine("server address: "+server);

		ReturnResource.Client client = new ReturnResource.Client(cid, rid);
		ReturnResource.Response response;
		try {
			response = client.sendRequest(server);
		} catch (IOException e) {
			log.warning(e.getMessage());
			throw new ServerCommunicationError("returnResource(): "+e.getMessage());
		}
		switch (response.status) {
		case SUCCESS:
			log.info(cid+" return "+rid+" - success");
			break;
		case NO_SUCH_RESOURCE:
			log.warning(response.message);
			throw new NoSuchResourceException(response.message);
		case NOT_BORROWED:
			log.warning(response.message);
			throw new NotBorrowedException(response.message);
		default:
			String msg = "Failed to return resource: "+response.message;
			log.warning(msg);
			throw new ServerCommunicationError("returnResource(): "+msg);
		}
	}

	void swapResource(CoordinatorID cid, ResourceID oldRID, ResourceID newRID) throws ServerCommunicationError, NoSuchResourceException {
		log.info(cid+": swap "+oldRID+", "+newRID);

		InetAddress server = servers.get(new City(oldRID.city));
		if (server == null) {
			String msg = "no connection to server "+oldRID.city;
			log.warning(msg);
			throw new ServerCommunicationError("swapResource(): "+msg);
		}
		log.fine("server address: "+server);

		SwapResource.Client client = new SwapResource.Client(cid, oldRID, newRID);
		SwapResource.Response response;
		try {
			response = client.sendRequest(server);
		} catch (IOException e) {
			throw new ServerCommunicationError("swapResource(): "+e.getMessage());
		}
		switch (response.status) {
		case SUCCESS:
			log.info(cid+": swap "+oldRID+", "+newRID+" - success");
			break;
		case NO_SUCH_RESOURCE:
			throw new NoSuchResourceException(response.message);
		default:
			throw new ServerCommunicationError("swapResource(): "+response.message);
		}
	}
}
