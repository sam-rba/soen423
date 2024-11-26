package derms.frontend;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style = Style.RPC)
public interface DERMSInterface {
    String addResource(String resourceID, String resourceName, int duration);
    String removeResource(String resourceID, int duration);
    String listResourceAvailability(String resourceName);

    // Coordinator methods
    String requestResource(String coordinatorID, String resourceID, int duration);
    String findResource(String coordinatorID, String resourceName);
    String returnResource(String coordinatorID, String resourceID);
    String swapResource(String coordinatorID, String oldResourceID, String oldResourceType, String newResourceID, String newResourceType);
}