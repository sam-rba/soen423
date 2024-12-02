//Resource.java
package derms.Replica3pkg;

import java.io.Serializable;

public class Resource implements Serializable {
    private static final long serialVersionUID = 1L;

    private String resourceName;
    private int duration;

    public Resource(String resourceName, int duration) {
        this.resourceName = resourceName.toUpperCase();
        this.duration = duration;
    }

    // Getters
    public String getResourceName() {
        return resourceName;
    }

    public synchronized int getDuration() {
        return duration;
    }

    // Setters
    public synchronized void setDuration(int duration) {
        this.duration = duration;
    }

    // Methods to manipulate duration
    public synchronized void addDuration(int additionalDuration) {
        if (additionalDuration > 0) {
            this.duration += additionalDuration;
        }
    }

    public synchronized void subDuration(int decrement) {
        if (decrement > 0 && this.duration >= decrement) {
            this.duration -= decrement;
        }
    }
}