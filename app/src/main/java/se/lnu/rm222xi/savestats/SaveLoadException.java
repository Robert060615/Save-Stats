package se.lnu.rm222xi.savestats;

/**
 * Thrown when a save file cannot be loaded. The message is written for the user and names the
 * file.
 */
public class SaveLoadException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Creates an exception with a message that can be shown to the user.
   *
   * @param message what went wrong, including the file name
   * @param cause the underlying error
   */
  SaveLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
