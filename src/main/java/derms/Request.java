package derms;

import derms.net.MessagePayload;

public class Request implements MessagePayload {
    private String function = "";
    private String clientID = "";
    private String resourceType = "";
    private String OldResourceType = "";
    private String resourceID = "";
    private String OldResourceID = "";
    private String FeIpAddress = "FE.FE_IP_Address";
    private int duration = 0;
    private int sequenceNumber = 0;
    private String MessageType = "00";
    private int retryCount = 1;
    private int reqId;

    public Request(String function, String clientID) {
        setFunction(function);
        setClientID(clientID);
    }

    @Override
    public int hash() {
        return function.hashCode() + clientID.hashCode() + resourceType.hashCode()
                + OldResourceType.hashCode() + resourceID.hashCode() + duration + sequenceNumber
                + MessageType.hashCode();
    }

    public Request(int rmNumber, String bugType) {
        setMessageType(bugType + rmNumber);
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getOldResourceType() {
        return OldResourceType;
    }

    public void setOldResourceType(String OldResourceType) {
        this.OldResourceType = OldResourceType;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getOldResourceID() {
        return OldResourceID;
    }

    public void setOldResourceID(String OldResourceID) {
        this.OldResourceID = OldResourceID;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String noRequestSendError() {
        return "request: " + getFunction() + " from " + getClientID() + " not sent";
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getFeIpAddress() {
        return FeIpAddress;
    }

    public void setFeIpAddress(String feIpAddress) {
        FeIpAddress = feIpAddress;
    }

    public String getMessageType() {
        return MessageType;
    }

    public void setMessageType(String messageType) {
        MessageType = messageType;
    }

    public boolean haveRetries() {
        return retryCount > 0;
    }

    public void countRetry() {
        retryCount--;
    }

    public int getId() { return reqId; }

    public void setId(int id) { reqId = id; }

    //Message Format: Sequence_id;FrontIpAddress;Message_Type;function(addResource,...);userID; newEventID;newEventType; oldEventID; oldEventType;bookingCapacity
    @Override
    public String toString() {
        return getSequenceNumber() + ";" +
                getFeIpAddress().toUpperCase() + ";" +
                getMessageType().toUpperCase() + ";" +
                getFunction().toUpperCase() + ";" +
                getClientID().toUpperCase() + ";" +
                getResourceID().toUpperCase() + ";" +
                getResourceType().toUpperCase() + ";" +
                getOldResourceID().toUpperCase() + ";" +
                getOldResourceType().toUpperCase() + ";" +
                getDuration();
    }
}
