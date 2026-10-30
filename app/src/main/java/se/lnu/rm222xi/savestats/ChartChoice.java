package se.lnu.rm222xi.savestats;

/**
 * The charts that can be ticked on the start page, each with the value its checkbox sends.
 */
enum ChartChoice {
  PARTY_LEVELS("partyLevels"),
  POKEDEX("pokedex"),
  TEAM("team"),
  PROGRESS("progress");

  private final String formValue;

  ChartChoice(String formValue) {
    this.formValue = formValue;
  }

  /**
   * Finds the chart that a checkbox value stands for.
   *
   * @param formValue the value sent by the checkbox, for example {@code "pokedex"}
   * @return the matching chart
   * @throws InvalidRequestException if no chart has that value
   */
  static ChartChoice fromFormValue(String formValue) throws InvalidRequestException {
    for (ChartChoice choice : values()) {
      if (choice.formValue.equals(formValue)) {
        return choice;
      }
    }

    throw new InvalidRequestException("Unknown chart: " + formValue);
  }
}
