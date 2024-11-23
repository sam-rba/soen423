package derms.replica.replica1;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ResponderClient extends Client<Responder, ResponderServer> {
	public ResponderID id;

	public ResponderClient(ResponderID id) {
		super(Responder.class, ResponderServer.class);
		this.id = id;
	}

	public ResponderClient(City city, short idNum) {
		this(new ResponderID(city, idNum));
	}

	public void add(ResourceID rid, ResourceName name, int duration) throws UnknownHostException, MalformedURLException {
		Responder server = connect(new City(rid.city));
		server.addResource(new Resource(rid, name, duration));
	}

	public void remove(ResourceID rid, int duration) throws UnknownHostException, MalformedURLException, NoSuchResourceException {
		Responder server = connect(new City(rid.city));
		server.removeResource(rid, duration);
	}

	public Resource[] listResources(ResourceName name) throws UnknownHostException, MalformedURLException, ServerCommunicationError {
		Responder server = connect(id.city);
		return server.listResourceAvailability(name);
	}
}
