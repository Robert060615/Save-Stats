package se.lnu.rm222xi.savestats;

import java.io.PrintStream;

/**
 * Everything the user sees in the terminal.
 */
class ConsoleView {

  private final PrintStream output;

  /**
   * Creates a view that writes to the given stream.
   *
   * @param output where text is written, usually {@code System.out}
   */
  ConsoleView(PrintStream output) {
    this.output = output;
  }

  void showUsage() {
    output.println("Usage: java -jar save-stats.jar <save file> [more save files, oldest first]");
    output.println("Example: java -jar save-stats.jar samples/red.sav");
  }

  void showLoadedSave(int saveNumber, GameSave save) {
    output.println("Save " + saveNumber + ": " + save.getFileName()
        + " (trainer " + save.getTrainerName() + ")");
  }

  void showMessage(String message) {
    output.println(message);
  }

  void showError(String message) {
    output.println("Error: " + message);
  }
}
