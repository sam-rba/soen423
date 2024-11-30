// DERMSImpl.java
package derms.frontend;

//import logger.Logger;
//import model.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

//import constants.Constants;
//import constants.PortConstants;
//import validation.ValidationService;
//import interfaces.DERMSInterface;
import  derms.Request;
import  derms.Response;
@WebService(endpointInterface = "derms.frontend.DERMSInterface")
public class DERMSServerImpl implements DERMSInterface {
	
    private static long DYNAMIC_TIMEOUT = 10000;
    private static int Rm1BugCount = 0;
    private static int Rm2BugCount = 0;
    private static int Rm3BugCount = 0;
    private static int Rm4BugCount = 0;
    private static int Rm1NoResponseCount = 0;
    private static int Rm2NoResponseCount = 0;
    private static int Rm3NoResponseCount = 0;
    private static int Rm4NoResponseCount = 0;
    private long responseTime = DYNAMIC_TIMEOUT;
    private long startTime;
    private CountDownLatch latch;
    private FEInterface inter = null;
    private final List<Response> responses = new ArrayList<>();

    public DERMSServerImpl() {
        super();
    }
    
    public DERMSServerImpl(FEInterface inter) {
        super();
        this.inter = inter;
    }
    
	@Override
    public synchronized String addResource(String resourceID, String resourceName, int duration) {
        System.out.println("FE Implementation:addEvent>>> called the method" );
        Request request = new Request("addResource", "");
        request.setResourceID(resourceID);
        request.setResourceType(resourceName);
        request.setDuration(duration);
        System.out.println("FE Implementation:addEvent>>> set values" );
        request.setSequenceNumber(sendUdpUnicastToSequencer(request));
        System.out.println("FE Implementation:addEvent>>>" + request.toString());
        return validateResponses(request);
    }

	@Override
    public synchronized String removeResource(String resourceID, int duration) {
        Request request = new Request("removeResource", "");
        request.setResourceID(resourceID);
        request.setDuration(duration);
        request.setSequenceNumber(sendUdpUnicastToSequencer(request));
        System.out.println("FE Implementation:removeEvent>>>" + request.toString());
        return validateResponses(request);
    }

	@Override
    public synchronized String listResourceAvailability(String resourceName) {
        Request request = new Request("listEventAvailability", "");
        request.setResourceType(resourceName);
        request.setSequenceNumber(sendUdpUnicastToSequencer(request));
        System.out.println("FE Implementation:listEventAvailability>>>" + request.toString());
        return validateResponses(request);
    }

	@Override
    public synchronized String requestResource(String coordinatorID, String resourceID, int duration) {
        Request request = new Request("requestResource", coordinatorID);
        request.setResourceID(resourceID);
        request.setDuration(duration);
        request.setSequenceNumber(sendUdpUnicastToSequencer(request));
        System.out.println("FE Implementation:bookEvent>>>" + request.toString());
        return validateResponses(request);
    }
	
	@Override
    public synchronized String findResource(String coordinatorID, String resourceName) {
        Request request = new Request("findResource", coordinatorID);
        request.setResourceType(resourceName);
        request.setSequenceNumber(sendUdpUnicastToSequencer(request));
        System.out.println("FE Implementation:bookEvent>>>" + request.toString());
        return validateResponses(request);
    }
	
	@Override
    public synchronized String returnResource(String coordinatorID, String resourceID) {
        Request request = new Request("findResource", coordinatorID);
        request.setResourceID(resourceID);
        request.setSequenceNumber(sendUdpUnicastToSequencer(request));
        System.out.println("FE Implementation:bookEvent>>>" + request.toString());
        return validateResponses(request);
    }

	@Override
    public synchronized String swapResource(String coordinatorID, String oldResourceID, String oldResourceType, String newResourceID, String newResourceType) {
        Request request = new Request("swapResource", coordinatorID);
        request.setResourceID(newResourceID);
        request.setResourceType(newResourceType);
        request.setOldResourceID(oldResourceID);
        request.setOldResourceType(oldResourceType);
        request.setSequenceNumber(sendUdpUnicastToSequencer(request));
        System.out.println("FE Implementation:swapEvent>>>" + request.toString());
        return validateResponses(request);
    }
	
    public void waitForResponse() {
        try {
            System.out.println("FE Implementation:waitForResponse>>>ResponsesRemain" + latch.getCount());
            boolean timeoutReached = latch.await(DYNAMIC_TIMEOUT, TimeUnit.MILLISECONDS);
            if (timeoutReached) {
                setDynamicTimout();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
//            inter.sendRequestToSequencer(request);
        }
//         check result and react correspondingly
    }
	
   private String validateResponses(Request request) {
        String resp;
        switch ((int) latch.getCount()) {
            case 0:
            case 1:
            case 2:
            case 3:
                resp = findMajorityResponse(request);
                break;
            case 4:
                resp = "Fail: No response from any server";
                System.out.println(resp);
                if (request.haveRetries()) {
                    request.countRetry();
                    resp = retryRequest(request);
                }
                rmDown(1);
                rmDown(2);
                rmDown(3);
                rmDown(4);
                break;
            default:
                resp = "Fail: " + request.noRequestSendError();
                break;
        }
        System.out.println("FE Implementation:validateResponses>>>Responses remain:" + latch.getCount() + " >>>Response to be sent to client " + resp);
        return resp;
    }
   
   private String findMajorityResponse(Request request) {
       Response res1 = null;
       Response res2 = null;
       Response res3 = null;
       Response res4 = null;
       for (Response response :responses) {
           if (response.getSequenceID() == request.getSequenceNumber()) {
               switch (response.getRmNumber()) {
                   case 1:
                       res1 = response;
                       break;
                   case 2:
                       res2 = response;
                       break;
                   case 3:
                       res3 = response;
                       break;
                   case 4:
                       res4 = response;
                       break;
               }
           }
       }
       System.out.println("FE Implementation:findMajorityResponse>>>RM1" + ((res1 != null) ? res1.getResponse() : "null"));
       System.out.println("FE Implementation:findMajorityResponse>>>RM2" + ((res2 != null) ? res2.getResponse() : "null"));
       System.out.println("FE Implementation:findMajorityResponse>>>RM3" + ((res3 != null) ? res3.getResponse() : "null"));
       System.out.println("FE Implementation:findMajorityResponse>>>RM4" + ((res4 != null) ? res4.getResponse() : "null"));

    // Handling null responses and marking RMs down
       if (res1 == null) rmDown(1);
       if (res2 == null) rmDown(2);
       if (res3 == null) rmDown(3);
       if (res4 == null) rmDown(4);

       // Majority voting logic
       List<Response> validResponses = Arrays.asList(res1, res2, res3, res4).stream()
                                              .filter(Objects::nonNull)
                                              .collect(Collectors.toList());

       Map<String, Long> responseCounts = validResponses.stream()
               .collect(Collectors.groupingBy(Response::getResponse, Collectors.counting()));

       // Find the response with the highest count
       Optional<Map.Entry<String, Long>> majorityEntry = responseCounts.entrySet().stream()
               .filter(entry -> entry.getValue() >= 3) // At least 3 matching responses for majority
               .findFirst();

       if (majorityEntry.isPresent()) {
           String majorityResponse = majorityEntry.get().getKey();
           System.out.println("Majority response found: " + majorityResponse);
           return majorityResponse;
       }

       // Handle bugs or discrepancies
       responseCounts.forEach((response, count) -> {
           if (count == 1) {
               validResponses.stream()
                       .filter(r -> r.getResponse().equals(response))
                       .forEach(r -> rmBugFound(r.getRmNumber()));
           }
       });

       return "Fail: majority response not found";
   }
   
   private void rmBugFound(int rmNumber) {
       switch (rmNumber) {
           case 1:
               Rm1BugCount++;
               if (Rm1BugCount == 3) {
                   Rm1BugCount = 0;
                   inter.informRmHasBug(rmNumber);
               }
               break;
           case 2:
               Rm2BugCount++;
               if (Rm2BugCount == 3) {
                   Rm2BugCount = 0;
                   inter.informRmHasBug(rmNumber);
               }
               break;

           case 3:
               Rm3BugCount++;
               if (Rm3BugCount == 3) {
                   Rm3BugCount = 0;
                   inter.informRmHasBug(rmNumber);
               }
               break;
           case 4:
               Rm4BugCount++;
               if (Rm4BugCount == 3) {
                   Rm4BugCount = 0;
                   inter.informRmHasBug(rmNumber);
               }
               break;
       }
       System.out.println("FE Implementation:rmBugFound>>>RM1 - bugs:" + Rm1BugCount);
       System.out.println("FE Implementation:rmBugFound>>>RM2 - bugs:" + Rm2BugCount);
       System.out.println("FE Implementation:rmBugFound>>>RM3 - bugs:" + Rm3BugCount);
       System.out.println("FE Implementation:rmBugFound>>>RM4 - bugs:" + Rm4BugCount);
   }
   
   private void rmDown(int rmNumber) {
       DYNAMIC_TIMEOUT = 10000;
       switch (rmNumber) {
           case 1:
               Rm1NoResponseCount++;
               if (Rm1NoResponseCount == 3) {
                   Rm1NoResponseCount = 0;
                   inter.informRmIsDown(rmNumber);
               }
               break;
           case 2:
               Rm2NoResponseCount++;
               if (Rm2NoResponseCount == 3) {
                   Rm2NoResponseCount = 0;
                   inter.informRmIsDown(rmNumber);
               }
               break;

           case 3:
               Rm3NoResponseCount++;
               if (Rm3NoResponseCount == 3) {
                   Rm3NoResponseCount = 0;
                   inter.informRmIsDown(rmNumber);
               }
               break;
           case 4:
               Rm4NoResponseCount++;
               if (Rm4NoResponseCount == 3) {
                   Rm4NoResponseCount = 0;
                   inter.informRmIsDown(rmNumber);
               }
               break;
       }
       System.out.println("FE Implementation:rmDown>>>RM1 - noResponse:" + Rm1NoResponseCount);
       System.out.println("FE Implementation:rmDown>>>RM2 - noResponse:" + Rm2NoResponseCount);
       System.out.println("FE Implementation:rmDown>>>RM3 - noResponse:" + Rm3NoResponseCount);
       System.out.println("FE Implementation:rmDown>>>RM4 - noResponse:" + Rm3NoResponseCount);
   }
   
   private void setDynamicTimout() {
       if (responseTime < 4000) {
           DYNAMIC_TIMEOUT = (DYNAMIC_TIMEOUT + (responseTime * 3)) / 2;
//           System.out.println("FE Implementation:setDynamicTimout>>>" + responseTime * 2);
       } else {
           DYNAMIC_TIMEOUT = 10000;
       }
       System.out.println("FE Implementation:setDynamicTimout>>>" + DYNAMIC_TIMEOUT);
   }

   private void notifyOKCommandReceived() {
       latch.countDown();
       System.out.println("FE Implementation:notifyOKCommandReceived>>>Response Received: Remaining responses" + latch.getCount());
   }

   public void addReceivedResponse(Response res) {
       long endTime = System.nanoTime();
       responseTime = (endTime - startTime) / 1000000;
       System.out.println("Current Response time is: " + responseTime);
       responses.add(res);
       notifyOKCommandReceived();
   }

   private int sendUdpUnicastToSequencer(Request request) {
       startTime = System.nanoTime();
       System.out.println("inside the udp unicast sequencer");
       int sequenceNumber = inter.sendRequestToSequencer(request);
       System.out.println("after its done the udp unicast sequencer");
       request.setSequenceNumber(sequenceNumber);
       latch = new CountDownLatch(4);
       waitForResponse();
       return sequenceNumber;
   }

   private String retryRequest(Request request) {
       System.out.println("FE Implementation:retryRequest>>>" + request.toString());
       startTime = System.nanoTime();
       inter.retryRequest(request);
       latch = new CountDownLatch(4);
       waitForResponse();
       return validateResponses(request);
   }

	
}

