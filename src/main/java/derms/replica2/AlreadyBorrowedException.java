package derms.replica2;

class AlreadyBorrowedException extends Exception {
  AlreadyBorrowedException(String message) {
    super(message);
  }
}