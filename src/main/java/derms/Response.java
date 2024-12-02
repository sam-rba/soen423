package derms;

import derms.net.MessagePayload;

public class Response implements MessagePayload {
    private int sequenceID = 0;
    private String response = "";
    private int rmNumber = 0;
    private String function = "";
    private String userID = "";
    private String newResourceID = "";
    private String newResourceType = "";
    private String oldResourceID = "";
    private String oldResourceType = "";
    private int duration = 0;
    private String udpMessage = "";
    private boolean isSuccess = false;
    private int requestId;

    public Response(String udpMessage) {
        setUdpMessage(udpMessage.trim());
        String[] messageParts = getUdpMessage().split(";");
        setSequenceID(Integer.parseInt(messageParts[0]));
        setResponse(messageParts[1].trim());
        setRmNumber(messageParts[2]);
        setFunction(messageParts[3]);
        setUserID(messageParts[4]);
        setNewResourceID(messageParts[5]);
        setNewResourceType(messageParts[6]);
        setOldResourceID(messageParts[7]);
        setOldResourceType(messageParts[8]);
        setDuration(Integer.parseInt(messageParts[9]));
    }

    public Response(int sequenceID, String status) {
        setSequenceID(sequenceID);
        setResponse(status);
        setRmNumber("2");
        setFunction("");
        setUserID("");
        setNewResourceID("");
        setNewResourceType("");
        setOldResourceID("");
        setOldResourceType("");
        setDuration(1);
    }

    public Response(Request req, int rmNumber, String response, boolean isSuccess) {
        this.sequenceID = req.getSequenceNumber();
        this.response = response;
        this.rmNumber = rmNumber;
        this.function = req.getFunction();
        this.isSuccess = isSuccess;
        this.requestId = req.getId();
    }

    @Override
    public int hash() {
        return  sequenceID + response.hashCode() + rmNumber + function.hashCode()
                + userID.hashCode() + newResourceID.hashCode() + newResourceType.hashCode()
                + oldResourceID.hashCode() + oldResourceType.hashCode() + duration
                + udpMessage.hashCode();
    }

    public int getSequenceID() {
        return sequenceID;
    }

    public void setSequenceID(int sequenceID) {
        this.sequenceID = sequenceID;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        isSuccess = response.contains("Success:");
        this.response = response;
    }

    public int getRmNumber() {
        return rmNumber;
    }

    public void setRmNumber(String rmNumber) {
        if (rmNumber.equalsIgnoreCase("RM1")) {
            this.rmNumber = 1;
        } else if (rmNumber.equalsIgnoreCase("RM2")) {
            this.rmNumber = 2;
        } else if (rmNumber.equalsIgnoreCase("RM3")) {
            this.rmNumber = 3;
        } else {
            this.rmNumber = 0;
        }
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getNewResourceID() {
        return newResourceID;
    }

    public void setNewResourceID(String newEventID) {
        this.newResourceID = newEventID;
    }

    public String getNewResourceType() {
        return newResourceType;
    }

    public void setNewResourceType(String newEventType) {
        this.newResourceType = newEventType;
    }

    public String getOldResourceID() {
        return oldResourceID;
    }

    public void setOldResourceID(String oldEventID) {
        this.oldResourceID = oldEventID;
    }

    public String getOldResourceType() {
        return oldResourceType;
    }

    public void setOldResourceType(String oldEventType) {
        this.oldResourceType = oldEventType;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int bookingCapacity) {
        this.duration = bookingCapacity;
    }

    public String getUdpMessage() {
        return udpMessage;
    }

    public void setUdpMessage(String udpMessage) {
        this.udpMessage = udpMessage;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public int getRequestId() { return requestId; }

    public void setRequestId(int id) { requestId = id; }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            if (obj instanceof Response) {
                Response obj1 = (Response) obj;
                return obj1.getFunction().equalsIgnoreCase(this.getFunction())
                        && obj1.getSequenceID() == this.getSequenceID()
                        && obj1.getUserID().equalsIgnoreCase(this.getUserID())
                        && obj1.isSuccess() == this.isSuccess();
//                        && obj1.getResponse().equalsIgnoreCase(this.getResponse());
            }
        }
        return false;
    }

    //Message Format: Sequence_id;FrontIpAddress;Message_Type;function(addResource,...);userID; newEventID;newEventType; oldEventID; oldEventType;bookingCapacity
    @Override
    public String toString() {
        return getResponse();
    }
}
