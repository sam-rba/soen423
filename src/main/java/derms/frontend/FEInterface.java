package derms.frontend;
import  derms.Request;

public interface FEInterface {
    void informRmHasBug(int replicaId);

    void informRmIsDown(int replicaId);

    int sendRequestToSequencer(Request request);

    void retryRequest(Request request);
}