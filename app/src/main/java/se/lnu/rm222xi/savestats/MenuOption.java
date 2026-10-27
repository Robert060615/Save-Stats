package se.lnu.rm222xi.savestats;

/**
 * The choices in the main menu, in the order they are shown.
 */
enum MenuOption {
  PARTY_LEVELS("Add chart: party levels"),
  POKEMON_STATS("Add chart: stats of one Pokémon"),
  POKEDEX_PROGRESS("Add chart: Pokédex progress"),
  SAVE_AND_QUIT("Save report and quit"),
  QUIT("Quit without saving");

  private final String description;

  MenuOption(String description) {
    this.description = description;
  }

  String getDescription() {
    return description;
  }

  boolean endsSession() {
    return this == SAVE_AND_QUIT || this == QUIT;
  }
}
