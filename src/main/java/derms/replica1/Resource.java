package derms.replica1;

import java.util.LinkedList;
import java.util.List;

public class Resource {
    private String resourceID;
    private String resourceName;
    private int duration;
    public boolean borrowed = false;
    public String borrower;
    public List<Requester> requesters = new LinkedList<>();

    public Resource(String resourceID, String resourceName, int duration) {
        this.resourceID = resourceID;
        this.resourceName = resourceName;
        this.duration = duration;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return resourceID + " - " + resourceName + " - " + duration;
    }
}