package derms.replica2;

import derms.Replica;
import derms.ReplicaManager;
import derms.Request;
import derms.Response;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Replica2 implements Replica {
	static final InetSocketAddress announceGroup = new InetSocketAddress("225.5.5.5", 5555);

	final City city;
	final InetAddress localAddr;
	final Resources resources;
	final Servers servers;
	private final Logger log;
	private final ResponderServer responderServer;
	private final CoordinatorServer coordinatorServer;
	private boolean alive;
	private final ReplicaManager replicaManager;

	public Replica2(City city, ReplicaManager replicaManager) throws IOException {
		this.city = city;
		this.localAddr = InetAddress.getLocalHost();
		this.resources = new Resources();
		this.servers = new Servers();
		this.log = DermsLogger.getLogger(getClass());
		this.replicaManager = replicaManager;
		try {
			this.responderServer = new ResponderServer(city, resources, servers);
		} catch (IOException e) {
			throw new IOException("Failed to create ResponderServer: "+e.getMessage());
		}
		log.info("Created ResponderServer");

		try {
			this.coordinatorServer = new CoordinatorServer(city, resources, servers);
		} catch (IOException e) {
			throw new IOException("Failed to create CoordinatorServer: "+e.getMessage());
		}
		log.info("Created CoordinatorServer");

		log.info("Running");
		log.config("Local address is "+localAddr.toString());

		ExecutorService pool = Executors.newCachedThreadPool();

		try {
			pool.execute(new ResourceAvailability.Server(localAddr, resources));
		} catch (IOException e) {
			String msg = "Failed to start ResourceAvailability Server: "+e.getMessage();
			log.severe(msg);
			throw e;
		}
		try {
			pool.execute(new RequestResource.Server(localAddr, resources));
		} catch (IOException e) {
			log.severe("Failed to start RequestResource Server: "+e.getMessage());
			throw e;
		}
		try {
			pool.execute(new FindResource.Server(localAddr, resources));
		} catch (IOException e) {
			log.severe("Failed to start FindResource Server: "+e.getMessage());
			throw e;
		}
		try {
			pool.execute(new ReturnResource.Server(localAddr, resources));
		} catch (IOException e) {
			log.severe("Failed to start ReturnResource Server: "+e.getMessage());
			throw e;
		}
		try {
			pool.execute(new SwapResource.Server(localAddr, resources, servers));
		} catch (IOException e) {
			log.severe("Failed to start SwapResource Server: "+e.getMessage());
			throw e;
		}

		try {
			pool.execute(new Announcer(announceGroup, localAddr, city));
		} catch (IOException e) {
			log.severe("Failed to start Announcer: "+e.getMessage());
			throw e;
		}
		try {
			pool.execute(new AnnounceListener(announceGroup, localAddr, servers));
		} catch (IOException e) {
			log.severe("Failed to start AnnounceListener: "+e.getMessage());
			throw e;
		}

		this.alive = true;
	}

	@Override
	public boolean isAlive() { return alive; }

	@Override
	public void startProcess() {
		// TODO
		log.info(getClass().getSimpleName() + " started.");
	}

	@Override
	public void processRequest(Request request) {
		log.info(request.toString());

		String status = "";
		try {
			switch (request.getFunction()) {
				case "addResource":
					status = addResource(request);
					break;
				case "removeResource":
					status = removeResource(request);
					break;
				case "listResourceAvailability":
					status = listResourceAvailability(request);
					break;
				case "requestResource":
					status = requestResource(request);
					break;
				case "findResource":
					status = findResource(request);
					break;
				case "returnResource":
					status = returnResource(request);
					break;
				case "swapResource":
					status = swapResource(request);
					break;
				default:
					status = "Failure: unknown function '" + request.getFunction() + "'";
			}
		} catch (Exception e) {
			log.warning(e.getMessage());
			status = "Failure: " + request.getFunction() + ": " + e.getMessage();
		}

		Response response = new Response(request.getSequenceNumber(), status);
		log.info("Processed request " + request + "; response: " + response);
		replicaManager.sendResponseToFE(response);
	}

	@Override
	public void restart() {
		// TODO
		shutdown();
		startProcess();
	}

	@Override
	public int getId() { return 2; }

	private void shutdown() {
		// TODO
	}

	private String addResource(Request request) {
		Resource resource = new Resource(
				ResourceID.parse(request.getResourceID()),
				ResourceType.parse(request.getResourceType()),
				request.getDuration());
		responderServer.addResource(resource);
		return "Successfully added resource " + resource;
	}

	private String removeResource(Request request) {
		try {
			responderServer.removeResource(
					ResourceID.parse(request.getResourceID()),
					request.getDuration());
			return "Successfully removed resource " + request.getResourceID();
		} catch (NoSuchResourceException e) {
			String msg = "Error removing " + request.getResourceID() + ": " + e.getMessage();
			log.warning(msg);
			return msg;
		}
	}

	private String listResourceAvailability(Request request) {
		// TODO
		throw new NotImplementedException();
	}

	private String requestResource(Request request) {
		try {
			coordinatorServer.requestResource(
					CoordinatorID.parse(request.getClientID()),
					ResourceID.parse(request.getResourceID()),
					request.getDuration());
			return "Successfully borrowed " + request.getResourceID();
		} catch (NoSuchResourceException | AlreadyBorrowedException | InvalidDurationException |ServerCommunicationError e) {
            String msg = "Failed to borrow resource " + request.getResourceID() + ": " + e.getMessage();
			log.warning(msg);
			return msg;
        }
    }

	private String findResource(Request request) {
		// TODO
		throw new NotImplementedException();
	}

	private String returnResource(Request request) {
		try {
			coordinatorServer.returnResource(
					CoordinatorID.parse(request.getClientID()),
					ResourceID.parse(request.getResourceID()));
			return "Successfully returned resource " + request.getResourceID();
		} catch (NoSuchResourceException | NotBorrowedException | ServerCommunicationError e) {
            String msg = "Failed to borrow resource " + request.getResourceID() + ": " + e.getMessage();
			log.warning(msg);
			return msg;
        }
    }

	private String swapResource(Request request) {
		try {
			coordinatorServer.swapResource(
					CoordinatorID.parse(request.getClientID()),
					ResourceID.parse(request.getOldResourceID()),
					ResourceID.parse(request.getResourceID()));
			return "Successfully swapped " + request.getOldResourceID() + " for " + request.getResourceID();
		} catch (NoSuchResourceException | ServerCommunicationError e) {
            String msg = "Failed to swap " + request.getOldResourceID() + " for " + request.getResourceID() + ": " + e.getMessage();
			log.warning(msg);
			return msg;
        }
    }
}
