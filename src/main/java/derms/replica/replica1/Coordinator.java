package derms.replica.replica1;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface Coordinator {
    void requestResource(CoordinatorID cid, ResourceID rid, int duration)
            throws ServerCommunicationError, NoSuchResourceException,
            AlreadyBorrowedException, InvalidDurationException;

    Resource[] findResource(CoordinatorID cid, ResourceName rname)
            throws ServerCommunicationError;

    void returnResource(CoordinatorID cid, ResourceID rid)
            throws ServerCommunicationError, NoSuchResourceException,
            NotBorrowedException;

    void swapResource(CoordinatorID cid, ResourceID oldRID, ResourceID newRID)
            throws ServerCommunicationError, NoSuchResourceException;
}
