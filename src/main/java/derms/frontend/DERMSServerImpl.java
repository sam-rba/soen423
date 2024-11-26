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

@WebService(endpointInterface = "frontend.DERMSInterface")
@SOAPBinding(style = Style.RPC)
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
    private final FEInterface inter;
    private final List<RmResponse> responses = new ArrayList<>();
    
    public DERMSServerImpl(FEInterface inter) {
        super();
        this.inter = inter;
    }
    
	@WebMethod
    public synchronized String addResource(String resourceID, String resourceName, int duration) {
        MyRequest myRequest = new MyRequest("addResource", "");
        myRequest.setResourceID(resourceID);
        myRequest.setResourceType(resourceName);
        myRequest.setDuration(duration);
        myRequest.setSequenceNumber(sendUdpUnicastToSequencer(myRequest));
        System.out.println("FE Implementation:addEvent>>>" + myRequest.toString());
        return validateResponses(myRequest);
    }

	@WebMethod
    public synchronized String removeResource(String resourceID, int duration) {
        MyRequest myRequest = new MyRequest("removeResource", "");
        myRequest.setResourceID(resourceID);
        myRequest.setDuration(duration);
        myRequest.setSequenceNumber(sendUdpUnicastToSequencer(myRequest));
        System.out.println("FE Implementation:removeEvent>>>" + myRequest.toString());
        return validateResponses(myRequest);
    }

	@WebMethod
    public synchronized String listResourceAvailability(String resourceName) {
        MyRequest myRequest = new MyRequest("listEventAvailability", "");
        myRequest.setResourceType(resourceName);
        myRequest.setSequenceNumber(sendUdpUnicastToSequencer(myRequest));
        System.out.println("FE Implementation:listEventAvailability>>>" + myRequest.toString());
        return validateResponses(myRequest);
    }

	@WebMethod
    public synchronized String requestResource(String coordinatorID, String resourceID, int duration) {
        MyRequest myRequest = new MyRequest("requestResource", coordinatorID);
        myRequest.setResourceID(resourceID);
        myRequest.setDuration(duration);
        myRequest.setSequenceNumber(sendUdpUnicastToSequencer(myRequest));
        System.out.println("FE Implementation:bookEvent>>>" + myRequest.toString());
        return validateResponses(myRequest);
    }
	
	@WebMethod
    public synchronized String findResource(String coordinatorID, String resourceName) {
        MyRequest myRequest = new MyRequest("findResource", coordinatorID);
        myRequest.setResourceType(resourceName);
        myRequest.setSequenceNumber(sendUdpUnicastToSequencer(myRequest));
        System.out.println("FE Implementation:bookEvent>>>" + myRequest.toString());
        return validateResponses(myRequest);
    }
	
	@WebMethod
    public synchronized String returnResource(String coordinatorID, String resourceID) {
        MyRequest myRequest = new MyRequest("findResource", coordinatorID);
        myRequest.setResourceID(resourceID);
        myRequest.setSequenceNumber(sendUdpUnicastToSequencer(myRequest));
        System.out.println("FE Implementation:bookEvent>>>" + myRequest.toString());
        return validateResponses(myRequest);
    }

	@WebMethod
    public synchronized String swapResource(String coordinatorID, String oldResourceID, String oldResourceType, String newResourceID, String newResourceType) {
        MyRequest myRequest = new MyRequest("swapResource", coordinatorID);
        myRequest.setResourceID(newResourceID);
        myRequest.setResourceType(newResourceType);
        myRequest.setOldResourceID(oldResourceID);
        myRequest.setOldResourceType(oldResourceType);
        myRequest.setSequenceNumber(sendUdpUnicastToSequencer(myRequest));
        System.out.println("FE Implementation:swapEvent>>>" + myRequest.toString());
        return validateResponses(myRequest);
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
//            inter.sendRequestToSequencer(myRequest);
        }
//         check result and react correspondingly
    }
	
   private String validateResponses(MyRequest myRequest) {
        String resp;
        switch ((int) latch.getCount()) {
            case 0:
            case 1:
            case 2:
            case 3:
                resp = findMajorityResponse(myRequest);
                break;
            case 4:
                resp = "Fail: No response from any server";
                System.out.println(resp);
                if (myRequest.haveRetries()) {
                    myRequest.countRetry();
                    resp = retryRequest(myRequest);
                }
                rmDown(1);
                rmDown(2);
                rmDown(3);
                rmDown(4);
                break;
            default:
                resp = "Fail: " + myRequest.noRequestSendError();
                break;
        }
        System.out.println("FE Implementation:validateResponses>>>Responses remain:" + latch.getCount() + " >>>Response to be sent to client " + resp);
        return resp;
    }
   
   private String findMajorityResponse(MyRequest myRequest) {
       RmResponse res1 = null;
       RmResponse res2 = null;
       RmResponse res3 = null;
       RmResponse res4 = null;
       for (RmResponse response :responses) {
           if (response.getSequenceID() == myRequest.getSequenceNumber()) {
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
       List<RmResponse> validResponses = Arrays.asList(res1, res2, res3, res4).stream()
                                              .filter(Objects::nonNull)
                                              .collect(Collectors.toList());

       Map<String, Long> responseCounts = validResponses.stream()
               .collect(Collectors.groupingBy(RmResponse::getResponse, Collectors.counting()));

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

   public void addReceivedResponse(RmResponse res) {
       long endTime = System.nanoTime();
       responseTime = (endTime - startTime) / 1000000;
       System.out.println("Current Response time is: " + responseTime);
       responses.add(res);
       notifyOKCommandReceived();
   }

   private int sendUdpUnicastToSequencer(MyRequest myRequest) {
       startTime = System.nanoTime();
       int sequenceNumber = inter.sendRequestToSequencer(myRequest);
       myRequest.setSequenceNumber(sequenceNumber);
       latch = new CountDownLatch(4);
       waitForResponse();
       return sequenceNumber;
   }

   private String retryRequest(MyRequest myRequest) {
       System.out.println("FE Implementation:retryRequest>>>" + myRequest.toString());
       startTime = System.nanoTime();
       inter.retryRequest(myRequest);
       latch = new CountDownLatch(4);
       waitForResponse();
       return validateResponses(myRequest);
   }

	
}

