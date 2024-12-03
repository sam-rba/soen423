package derms.replica1;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.List;

@WebService
public interface DERMSInterface {
    @WebMethod
    String addResource(@WebParam(name = "resourceID") String resourceID,
                       @WebParam(name = "resourceName") String resourceName,
                       @WebParam(name = "duration") int duration);

    @WebMethod
    String removeResource(@WebParam(name = "resourceID") String resourceID,
                          @WebParam(name = "duration") int duration);

    @WebMethod
    List<String> listResourceAvailability(@WebParam(name = "resourceName") String resourceName);

    @WebMethod
    String requestResource(@WebParam(name = "coordinatorID") String coordinatorID,
                           @WebParam(name = "resourceID") String resourceID,
                           @WebParam(name = "duration") int duration);

    @WebMethod
    List<String> findResource(@WebParam(name = "coordinatorID") String coordinatorID,
                              @WebParam(name = "resourceName") String resourceName);

    @WebMethod
    String returnResource(@WebParam(name = "coordinatorID") String coordinatorID,
                          @WebParam(name = "resourceID") String resourceID);

    @WebMethod
    String swapResource(@WebParam(name = "coordinatorID") String coordinatorID,
                        @WebParam(name = "oldResourceID") String oldResourceID,
                        @WebParam(name = "oldResourceType") String oldResourceType,
                        @WebParam(name = "newResourceID") String newResourceID,
                        @WebParam(name = "newResourceType") String newResourceType);
}