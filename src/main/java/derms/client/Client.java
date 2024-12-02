package derms.client;

import derms.frontend.DERMSInterface;
import derms.frontend.DERMSServerImpl;
import derms.frontend.FE;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

class Client {
    public static final String namespace = "frontend.derms";
    public static final QName qname = new QName("http://"+namespace+"/", DERMSServerImpl.class.getSimpleName()+"Service");

    static DERMSInterface connectToServer(String FEhost) throws MalformedURLException {
        URL url = new URL(FE.endpointURL(FEhost) + "?wsdl");
        return Service.create(url, qname).getPort(DERMSInterface.class);
    }
}
