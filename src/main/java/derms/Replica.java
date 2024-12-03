package derms;

public interface Replica {
    boolean isAlive();
    void startProcess(int byzantine, int crash);
    void processRequest(Request request);
    void restart();
    int getId();
}
