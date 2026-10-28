package se.lnu.rm222xi.savestats;

import java.io.IOException;

/**
 * Starts Save Stats from the command line, either in the terminal or as a local web server.
 */
public class App {

  private static final String WEB_FLAG = "--web";
  private static final int WEB_PORT = 8080;

  /**
   * Execution entry point. Only wires the application to the terminal or the web server and hands
   * over control.
   *
   * @param args {@code --web} to start the web server, otherwise paths to one or more save files,
   *     oldest first
   */
  public static void main(String[] args) {
    ConsoleView view = new ConsoleView(System.in, System.out);
    if (args.length > 0 && WEB_FLAG.equals(args[0])) {
      startWebServer(view);
    } else {
      new SaveStatsApp(view, new SaveLoader()).run(args);
    }
  }

  private static void startWebServer(ConsoleView view) {
    try {
      WebServer server = new WebServer(WEB_PORT);
      server.start();
      view.showMessage("Save Stats is running at " + server.address());
      view.showMessage("Open it in a web browser. Press Ctrl+C to stop.");
    } catch (IOException e) {
      view.showError("Could not start the web server on port " + WEB_PORT + ": " + e.getMessage());
    }
  }
}
