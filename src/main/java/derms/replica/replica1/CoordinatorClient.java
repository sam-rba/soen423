package derms.replica.replica1;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

public class CoordinatorClient extends Client<Coordinator, CoordinatorServer> {
	public CoordinatorID id;
	public Coordinator server;

	public CoordinatorClient(CoordinatorID id) throws UnknownHostException, MalformedURLException {
		super(Coordinator.class, CoordinatorServer.class);
		this.id = id;
		this.server = connect(new City(id.city));
	}

	public CoordinatorClient(City city, short idNum) throws UnknownHostException, MalformedURLException {
		this(new CoordinatorID(city.toString(), idNum));
	}

	public void requestResource(ResourceID resourceID, int duration)
			throws ServerCommunicationError, NoSuchResourceException,
			AlreadyBorrowedException, InvalidDurationException
	{
		server.requestResource(id, resourceID, duration);
	}

	public void returnResource(ResourceID resourceID)
			throws ServerCommunicationError, NoSuchResourceException, NotBorrowedException
	{
		server.returnResource(id, resourceID);
	}

	public Resource[] findResource(ResourceName name) throws ServerCommunicationError {
		return server.findResource(id, name);
	}

	public void swapResource(ResourceID oldRID, ResourceID newRID) throws ServerCommunicationError, NoSuchResourceException {
		server.swapResource(id, oldRID, newRID);
	}
}