package derms.replica1;

class Requester {
    private String coordinatorID;
    private int duration;

    public Requester(String coordinatorID, int duration) {
        this.coordinatorID = coordinatorID;
        this.duration = duration;
    }

    public String getCoordinatorID() {
        return coordinatorID;
    }

    public void setCoordinatorID(String coordinatorID) {
        this.coordinatorID = coordinatorID;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration)  {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return String.format("derms.Requester{CoordinatorID=%s, Duration=%d}", coordinatorID, duration);
    }
}