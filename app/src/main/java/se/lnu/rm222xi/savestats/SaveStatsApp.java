package se.lnu.rm222xi.savestats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Runs one session: loads the save files given on the command line and lets the user pick charts
 * from the menu until they quit.
 */
class SaveStatsApp {

  private static final Path REPORT_FILE = Path.of("save-stats-report.html");
  private final ConsoleView view;
  private final SaveLoader loader;
  private final ChartBuilder chartBuilder = new ChartBuilder();
  private final Report report = new Report();
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
      return;
    }

    try {
      runMenu();
    } catch (InputClosedException e) {
      view.showMessage("Input closed. The report was not saved.");
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

  private void runMenu() {
    MenuOption chosen;
    do {
      chosen = view.chooseMenuOption();
      handle(chosen);
    } while (!chosen.endsSession());
  }

  private void handle(MenuOption option) {
    switch (option) {
      case PARTY_LEVELS -> addPartyLevels();
      case POKEMON_STATS -> addPokemonStats();
      case POKEMON_IVS -> addPokemonIvs();
      case POKEDEX_PROGRESS -> addToReport(chartBuilder.pokedexProgress(chooseSave()));
      case PROGRESS_BETWEEN_SAVES -> addProgressBetweenSaves();
      case SAVE_AND_QUIT -> saveReport();
      case QUIT -> view.showMessage("Bye! The report was not saved.");
    }
  }

  private void addPartyLevels() {
    chooseSaveWithParty().ifPresent(save -> 
      addToReport(chartBuilder.partyLevels(save)));
  }

  private void addPokemonStats() {
    chooseSaveWithParty().ifPresent(save -> 
      addToReport(chartBuilder.stats(save, choosePokemon(save))));
  }

  private void addPokemonIvs() {
    chooseSaveWithParty().ifPresent(save -> 
      addToReport(chartBuilder.ivs(save, choosePokemon(save))));
  }

  private void addProgressBetweenSaves() {
    if (saves.size() < 2) {
      view.showError("Start the application with at least two save files to compare them.");
      return;
    }

    addToReport(chartBuilder.caughtOverTime(saves));
    addToReport(chartBuilder.badgesOverTime(saves));
  }

  private void saveReport() {
    if (report.isEmpty()) {
      view.showMessage("The report is empty, so nothing was saved.");
      return;
    }

    try {
      Files.writeString(REPORT_FILE, report.toHtml());
      view.showMessage("Report saved to " + REPORT_FILE.toAbsolutePath());
      view.showMessage("Open it in a web browser to see the charts.");
    } catch (IOException e) {
      view.showError("Could not save the report: " + e.getMessage());
    }
  }

  private GameSave chooseSave() {
    if (saves.size() == 1) {
      return saves.getFirst();
    }

    List<String> fileNames = saves.stream().map(GameSave::getFileName).toList();
    return saves.get(view.chooseFrom("Which save?", fileNames));
  }

  /**
   * Lets the user choose a save, like {@link #chooseSave}, but only returns it if its party has at
   * least one Pokémon. Otherwise the user is told why no chart can be drawn.
   *
   * @return the chosen save, or an empty Optional if its party is empty
   */
  private Optional<GameSave> chooseSaveWithParty() {
    GameSave save = chooseSave();
    if (!save.hasParty()) {
      view.showError("The party in " + save.getFileName() + " is empty.");
      return Optional.empty();
    }

    return Optional.of(save);
  }

  private int choosePokemon(GameSave save) {
    return view.chooseFrom("Which Pokémon?", save.getPartyNicknames());
  }

  private void addToReport(ReportSection section) {
    report.add(section);
    view.showMessage("Added \"" + section.heading() + "\" to the report.");
  }
}
