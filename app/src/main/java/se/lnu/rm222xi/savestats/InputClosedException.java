package se.lnu.rm222xi.savestats;

/**
 * Thrown when the user closes the input (for example with Ctrl+D) while the application waits for
 * an answer.
 */
class InputClosedException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  InputClosedException() {
    super("The input was closed");
  }
}
