package derms.client;

import derms.frontend.DERMSInterface;
import derms.frontend.DERMSServerImpl;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class Client {
    public static final String namespace = "frontend.derms";
    public static final int port = 8067;
    public static final QName qname = new QName("http://"+namespace+"/", DERMSServerImpl.class.getSimpleName()+"Service");

    protected final DERMSInterface server;

    protected Client(String FEhost) throws MalformedURLException {
        URL url = new URL("http://"+FEhost+":"+port+"/"+DERMSInterface.class.getSimpleName()+"?wsdl");
        this.server = Service.create(url, qname).getPort(DERMSInterface.class);
    }
}
