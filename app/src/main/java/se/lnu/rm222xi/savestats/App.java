package se.lnu.rm222xi.savestats;

/**
 * Starts Save Stats from the command line.
 */
public class App {

  /**
   * Execution entry point. Only wires the application to the terminal and hands over control.
   *
   * @param args paths to one or more save files, oldest first
   */
  public static void main(String[] args) {
    ConsoleView view = new ConsoleView(System.out);
    new SaveStatsApp(view, new SaveLoader()).run(args);
  }
}
