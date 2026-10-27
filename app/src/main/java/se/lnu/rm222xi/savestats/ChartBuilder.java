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
    return drawSection(new BarChart(createOptions()), title, save.getPartyNicknames(), save.getPartyLevels());
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

  ReportSection caughtOverTime(List<GameSave> saves) {
    List<Double> caughtCounts = saves.stream().map(save -> (double) save.getCaughtCount()).toList();
    return drawSection(new LineChart(createOptions()), "Pokémon caught per save", saveNumbersOf(saves),
        caughtCounts);
  }

  ReportSection badgesOverTime(List<GameSave> saves) {
    List<Double> badgeCounts = saves.stream().map(save -> (double) save.getBadgeCount()).toList();
    return drawSection(new LineChart(createOptions()), "Badges per save", saveNumbersOf(saves), badgeCounts);
  }

  private ReportSection drawBarSection(String title, Map<String, Double> valuesByLabel) {
    return drawSection(new BarChart(createOptions()), title,
        new ArrayList<>(valuesByLabel.keySet()), new ArrayList<>(valuesByLabel.values()));
  }

  private List<String> saveNumbersOf(List<GameSave> saves) {
    return IntStream.rangeClosed(1, saves.size()).mapToObj(number -> "Save " + number).toList();
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
