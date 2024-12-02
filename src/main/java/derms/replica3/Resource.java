package derms.replica3;

import java.io.Serializable;

public class Resource implements Serializable {
    private String resourceID;
    private String resourceName;
    private int duration;

    public Resource(String resourceID, String resourceName, int duration) {
        this.resourceID = resourceID;
        this.resourceName = resourceName;
        this.duration = duration;
    }

    public String getResourceID() {
        return resourceID;
    }

    public String getResourceName() {
        return resourceName;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}

