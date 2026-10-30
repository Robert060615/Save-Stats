package se.lnu.rm222xi.savestats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import se.lnu.rm222xi.svgcharts.BarChart;
import se.lnu.rm222xi.svgcharts.ChartOptions;
import se.lnu.rm222xi.svgcharts.LineChart;
import se.lnu.rm222xi.svgcharts.SvgChart;

/**
 * Turns save data into report sections with charts drawn by the svg-charts module. All charts get
 * the color chosen by the user.
 */
class ChartBuilder {

  private static final String DEFAULT_COLOR = "#1f77b4";

  private String color = DEFAULT_COLOR;

  /**
   * Sets the color of the bars and lines in charts built from now on. Charts already in the report
   * keep their color.
   *
   * @param color a hex color, for example {@code "#e4572e"}
   * @throws se.lnu.rm222xi.svgcharts.ChartDataException if the color is not a hex color
   */
  void setColor(String color) {
    createOptions().setColor(color);
    this.color = color;
  }

  ReportSection partyLevels(GameSave save) {
    String title = "Party levels - " + save.getTrainerName();
    return drawSection(new BarChart(createOptions()),
        title, save.getPartyNicknames(), save.getPartyLevels());
  }

  ReportSection pokedexProgress(GameSave save) {
    int caught = save.getCaughtCount();
    int seenOnly = save.getSeenCount() - caught;
    int notSeen = save.getPokedexSize() - save.getSeenCount();
    String title = "Pokédex - " + caught + " of " + save.getPokedexSize() + " caught";

    return drawSection(new BarChart(createOptions()), title,
        List.of("Caught", "Seen, not caught", "Not seen"),
        List.of((double) caught, (double) seenOnly, (double) notSeen));
  }

  ReportSection stats(GameSave save, int partyIndex) {
    String title = "Stats of " + save.getPartyNicknames().get(partyIndex);
    return drawBarSection(title, save.getStatsOf(partyIndex));
  }

  ReportSection ivs(GameSave save, int partyIndex) {
    String title = "IVs of " + save.getPartyNicknames().get(partyIndex) + " (0-15)";
    return drawBarSection(title, save.getIvsOf(partyIndex));
  }

  /**
   * Draws the number of Pokémon caught in each save. The section lists which file each
   * <i>Save 1</i>, <i>Save 2</i> … stands for, since the file names do not fit under the chart.
   * The badge chart is meant to come right after it and uses the same labels.
   *
   * @param saves the saves, in the order the user gave them
   * @return the chart with the list of saves as notes
   */
  ReportSection caughtOverTime(List<GameSave> saves) {
    List<Double> caughtCounts = saves.stream().map(save -> (double) save.getCaughtCount()).toList();
    ReportSection chart = drawSection(new LineChart(createOptions()),
        "Pokémon caught per save",
        saveNumbersOf(saves), caughtCounts);
    return new ReportSection(chart.heading(), chart.svg(), describeSaves(saves));
  }

  ReportSection badgesOverTime(List<GameSave> saves) {
    List<Double> badgeCounts = saves.stream().map(save -> (double) save.getBadgeCount()).toList();
    return drawSection(new LineChart(createOptions()),
        "Badges per save",
        saveNumbersOf(saves), badgeCounts);
  }

  private ReportSection drawBarSection(String title, Map<String, Double> valuesByLabel) {
    return drawSection(new BarChart(createOptions()), title,
        new ArrayList<>(valuesByLabel.keySet()), new ArrayList<>(valuesByLabel.values()));
  }

  private List<String> saveNumbersOf(List<GameSave> saves) {
    return IntStream.rangeClosed(1, saves.size()).mapToObj(number -> "Save " + number).toList();
  }

  private List<String> describeSaves(List<GameSave> saves) {
    List<String> labels = saveNumbersOf(saves);
    return IntStream.range(0, saves.size())
        .mapToObj(i -> labels.get(i) + ": " + saves.get(i).getDescription()).toList();
  }

  private ReportSection drawSection(SvgChart chart, String title, List<String> labels,
      List<Double> values) {
    chart.setTitle(title);
    chart.setLabels(labels);
    chart.setValues(values);
    return new ReportSection(title, chart.toSvg());
  }

  private ChartOptions createOptions() {
    ChartOptions options = new ChartOptions();
    options.setColor(color);
    return options;
  }
}
