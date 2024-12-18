package derms.replica2;

import derms.*;
import derms.util.TestLogger;
import derms.util.ThreadPool;
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
	private final ReplicaManager replicaManager;
	private final ExecutorService pool;
	private boolean alive = false;
	private boolean byzFailure;

	public Replica2(City city, ReplicaManager replicaManager) throws IOException {
		this.city = city;
		this.localAddr = InetAddress.getLocalHost();
		this.resources = new Resources();
		this.servers = new Servers();
		this.log = DermsLogger.getLogger(getClass());
		this.replicaManager = replicaManager;
		this.pool = Executors.newCachedThreadPool();

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
	}

	public Replica2(String city, ReplicaManager replicaManager) throws IOException {
		this(new City(city), replicaManager);
	}

	@Override
	public boolean isAlive() { return alive; }

	@Override
	public void startProcess(int byzantine, int crash) {
		// [TEST] Detect crash
        if (crash == 1) {
            alive = false;
        } else {
            alive = true;
        }

        // [TEST] Detect byzantine failure
        if (byzantine == 1) {
            byzFailure = true;
        } else {
            byzFailure = false;
        }

		try {
			pool.execute(new ResourceAvailability.Server(localAddr, resources));
		} catch (IOException e) {
			String msg = "Failed to start ResourceAvailability Server: "+e.getMessage();
			log.severe(msg);
			return;
		}
		try {
			pool.execute(new RequestResource.Server(localAddr, resources));
		} catch (IOException e) {
			log.severe("Failed to start RequestResource Server: "+e.getMessage());
			return;
		}
		try {
			pool.execute(new FindResource.Server(localAddr, resources));
		} catch (IOException e) {
			log.severe("Failed to start FindResource Server: "+e.getMessage());
			return;
		}
		try {
			pool.execute(new ReturnResource.Server(localAddr, resources));
		} catch (IOException e) {
			log.severe("Failed to start ReturnResource Server: "+e.getMessage());
			return;
		}
		try {
			pool.execute(new SwapResource.Server(localAddr, resources, servers));
		} catch (IOException e) {
			log.severe("Failed to start SwapResource Server: "+e.getMessage());
			return;
		}

		try {
			pool.execute(new Announcer(announceGroup, localAddr, city));
		} catch (IOException e) {
			log.severe("Failed to start Announcer: "+e.getMessage());
			return;
		}
		try {
			pool.execute(new AnnounceListener(announceGroup, localAddr, servers));
		} catch (IOException e) {
			log.severe("Failed to start AnnounceListener: "+e.getMessage());
			return;
		}

		log.info("Running");
		log.config("Local address is "+localAddr.toString());
		//alive = true;
		log.info(getClass().getSimpleName() + " started.");
	}

	@Override
	public void processRequest(Request request) {
		log.info(request.toString());

		// [TEST] Simulate byzantine failure (return incorrect value)
        if (byzFailure == true) {
            Response response = new Response(request, replicaManager.getReplicaId(), "BYZANTINE FAILURE", false);
            replicaManager.sendResponseToFE(response);
            return;
        }

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

		Response response = new Response(request, replicaManager.getReplicaId(), status, false); // TODO: isSuccess flag
		log.info("Processed request " + request + "; response: " + response);
		replicaManager.sendResponseToFE(response);
	}

	@Override
	public void restart() {
		shutdown();

		// [TEST] Restart process without byzantine failure or crash
		TestLogger.log("REPLICA 2: {RESTARTED}");
		startProcess(0, 0);
	}

	@Override
	public int getId() { return 2; }

	public void shutdown() {
		log.info("Shutting down...");
		ThreadPool.shutdown(pool, log);
		alive = false;
		log.info("Finished shutting down.");
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
		try {
			Resource[] resources = responderServer.listResourceAvailability(
					ResourceType.parse(request.getResourceType()));
			StringBuilder result = new StringBuilder();
			result.append(request.getResourceType());
			for (Resource resource : resources)
				result.append(" " + resource.id + "-" + resource.duration);
			return result.toString();
		} catch (ServerCommunicationError e) {
			String msg = "Error listing resources: " + e.getMessage();
			log.warning(msg);
			return msg;
		}
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
		try {
			Resource[] resources = coordinatorServer.findResource(
					CoordinatorID.parse(request.getClientID()),
					ResourceType.parse(request.getResourceType()));

			if (resources.length < 1)
				return "Resources of " + request.getResourceType() + " occupied by coordinator " + request.getClientID() + " are not found!";

			StringBuilder result = new StringBuilder();
			result.append(request.getResourceType());
			for (Resource resource : resources)
				result.append(" " + resource.id + "-" + resource.borrowDuration);
			return result.toString();
		} catch (ServerCommunicationError e) {
			String msg = "Error finding resources: " + e.getMessage();
			log.warning(msg);
			return msg;
		}
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
