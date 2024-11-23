package derms.replica.replica1;

public class AlreadyBorrowedException extends Exception {
  public AlreadyBorrowedException(String message) {
    super(message);
  }
}
