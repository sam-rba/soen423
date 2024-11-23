package derms.replica.replica1;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public abstract class Client<I, C> {
	public static final String namespace = "derms.samanthony.xyz";
	public static final int port = 8080;

	private final Class<I> endpointInterface;
	private final QName qname;

	protected Client(Class<I> endpointInterface, Class<C> endpointClass) {
		this.endpointInterface = endpointInterface;
		this.qname = new QName("http://"+namespace+"/", endpointClass.getSimpleName()+"Service");
	}

	protected I connect(String host) throws MalformedURLException {
		URL url = new URL("http://"+host+":"+port+"/"+endpointInterface.getSimpleName()+"?wsdl");
		return Service.create(url, qname).getPort(endpointInterface);
	}

	protected I connect(City city) throws UnknownHostException, MalformedURLException {
		String host = Hosts.get(city);
		return connect(host);
	}
}
