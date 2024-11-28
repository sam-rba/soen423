package derms;

import derms.net.MessagePayload;

// TODO
public class Response implements MessagePayload {

    @Override
    public int hash() {
        // TODO
        return  -1;
    }
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
}
