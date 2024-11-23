package derms.replica.replica1;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface Responder {
    void addResource(Resource r);

    void removeResource(ResourceID rid, int duration)
            throws NoSuchResourceException;

    Resource[] listResourceAvailability(ResourceName rname)
            throws ServerCommunicationError;
}
