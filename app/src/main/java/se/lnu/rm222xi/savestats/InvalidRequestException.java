package se.lnu.rm222xi.savestats;

/**
 * Thrown when the form data from the start page is missing something or cannot be read. The
 * message is written for the user.
 */
public class InvalidRequestException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Creates an exception with a message that can be shown to the user.
   *
   * @param message what is wrong with the request
   */
  InvalidRequestException(String message) {
    super(message);
  }
}
