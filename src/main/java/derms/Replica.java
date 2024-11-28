package derms;

public interface Replica {
    boolean isAlive();
    void startProcess();
    void processRequest(Request request);
    void restart();
    int getId();
}
