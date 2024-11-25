package derms.replica.replica1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class StationServer implements Runnable {
	public static final String usage = "Usage: java StationServer <city> <local address>";
	public static final InetSocketAddress announceGroup = new InetSocketAddress("225.5.5.5", 5555);

	public City city;
	public InetAddress localAddr;
	public Resources resources;
	public Servers servers;
	private Logger log;
	private ResponderServer responderServer;
	private CoordinatorServer coordinatorServer;

	public StationServer(City city, InetAddress localAddr) throws IOException {
		this.city = city;
		this.localAddr = localAddr;
		this.resources = new Resources();
		this.servers = new Servers();
		this.log = DermsLogger.getLogger(getClass());

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

	public static void main(String cmdlineArgs[]) {
		Args args = null;
		try {
			args = new Args(cmdlineArgs);
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.out.println(usage);
			System.exit(1);
		}

		try {
			(new StationServer(args.city, args.localAddr)).run();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	@Override
	public void run() {
		log.info("Running");
		log.config("Local address is "+localAddr.toString());

		ExecutorService pool = Executors.newCachedThreadPool();

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
	}

	private static class Args {
		private City city;
		private InetAddress localAddr;

		private Args(String[] args) throws IllegalArgumentException {
			if (args.length < 1) {
				throw new IllegalArgumentException("Missing argument 'city'");
			}
			city = new City(args[0]);

			if (args.length < 2) {
				throw new IllegalArgumentException("Missing argument 'local host'");
			}
			try {
				localAddr = InetAddress.getByName(args[1]);
			} catch (UnknownHostException | SecurityException e) {
				throw new IllegalArgumentException("Bad value of 'local host': "+e.getMessage());
			}
		}
	}
}