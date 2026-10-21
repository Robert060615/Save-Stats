package se.lnu.rm222xi.savestats;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs one session: loads the save files given on the command line.
 */
class SaveStatsApp {

  private final ConsoleView view;
  private final SaveLoader loader;
  private List<GameSave> saves = List.of();

  SaveStatsApp(ConsoleView view, SaveLoader loader) {
    this.view = view;
    this.loader = loader;
  }

  /**
   * Runs the application until the user quits.
   *
   * @param savePaths paths to the save files, oldest first
   */
  void run(String[] savePaths) {
    if (savePaths.length == 0) {
      view.showUsage();
      return;
    }

    saves = loadAll(savePaths);
    if (saves.isEmpty()) {
      view.showError("None of the files could be loaded.");
    }
  }

  private List<GameSave> loadAll(String[] savePaths) {
    List<GameSave> loaded = new ArrayList<>();
    for (String savePath : savePaths) {
      try {
        GameSave save = loader.load(Path.of(savePath));
        loaded.add(save);
        view.showLoadedSave(loaded.size(), save);
      } catch (SaveLoadException e) {
        view.showError(e.getMessage());
      }
    }

    return loaded;
  }
}
