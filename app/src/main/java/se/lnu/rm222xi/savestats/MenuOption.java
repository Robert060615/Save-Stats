package se.lnu.rm222xi.savestats;

/**
 * The choices in the main menu, in the order they are shown.
 */
enum MenuOption {
  PARTY_LEVELS("Add chart: party levels"),
  QUIT("Quit without saving");

  private final String description;

  MenuOption(String description) {
    this.description = description;
  }

  String getDescription() {
    return description;
  }

  boolean endsSession() {
    return this == QUIT;
  }
}
