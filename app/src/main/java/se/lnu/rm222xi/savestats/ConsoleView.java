package se.lnu.rm222xi.savestats;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Everything the user sees and types in the terminal. Keeps asking until the answer is valid, so
 * the rest of the application only gets valid choices.
 */
class ConsoleView {

  private final Scanner input;
  private final PrintStream output;

  /**
   * Creates a view that reads from and writes to the given streams.
   *
   * @param input where the user's answers come from, usually {@code System.in}
   * @param output where text is written, usually {@code System.out}
   */
  ConsoleView(InputStream input, PrintStream output) {
    this.input = new Scanner(input, StandardCharsets.UTF_8);
    this.output = output;
  }

  void showUsage() {
    output.println("Usage: java -jar save-stats.jar <save file> [more save files, oldest first]");
    output.println("Example: java -jar save-stats.jar samples/red.sav");
    output.println("Or start it in a web browser: java -jar save-stats.jar --web");
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

  /**
   * Shows the main menu and asks until the user picks one of the choices.
   *
   * @return the chosen menu option
   */
  MenuOption chooseMenuOption() {
    output.println();
    output.println("What do you want to do?");

    MenuOption[] options = MenuOption.values();
    for (int i = 0; i < options.length; i++) {
      output.println("  " + (i + 1) + ") " + options[i].getDescription());
    }

    return options[readNumberUpTo(options.length) - 1];
  }

  /**
   * Shows a numbered list and asks until the user picks one of the items.
   *
   * @param question the question shown above the list
   * @param items the items to choose from, at least one
   * @return the index of the chosen item, starting at 0
   */
  int chooseFrom(String question, List<String> items) {
    output.println(question);
    for (int i = 0; i < items.size(); i++) {
      output.println("  " + (i + 1) + ") " + items.get(i));
    }

    return readNumberUpTo(items.size()) - 1;
  }

  String ask(String question) {
    output.print(question + " ");
    return readLine().trim();
  }

  private int readNumberUpTo(int max) {
    while (true) {
      String answer = ask("Choose 1-" + max + ":");
      if (isNumberBetween(answer, 1, max)) {
        return Integer.parseInt(answer);
      }
      showError("Please enter a number from 1 to " + max + ".");
    }
  }

  private boolean isNumberBetween(String text, int min, int max) {
    if (!text.matches("\\d{1,9}")) {
      return false;
    }

    int number = Integer.parseInt(text);
    return number >= min && number <= max;
  }

  private String readLine() {
    if (!input.hasNextLine()) {
      throw new InputClosedException();
    }

    return input.nextLine();
  }
}
