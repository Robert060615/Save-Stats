package se.lnu.rm222xi.savestats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import se.lnu.rm222xi.svgcharts.BarChart;
import se.lnu.rm222xi.svgcharts.LineChart;
import se.lnu.rm222xi.svgcharts.SvgChart;

/**
 * Turns save data into report sections with charts drawn by the svg-charts module.
 */
class ChartBuilder {

  ReportSection partyLevels(GameSave save) {
    String title = "Party levels - " + save.getTrainerName();
    return drawSection(new BarChart(), title, save.getPartyNicknames(), save.getPartyLevels());
  }

  ReportSection pokedexProgress(GameSave save) {
    int caught = save.getCaughtCount();
    int seenOnly = save.getSeenCount() - caught;
    int notSeen = save.getPokedexSize() - save.getSeenCount();
    String title = "Pokédex - " + caught + " of " + save.getPokedexSize() + " caught";

    return drawSection(new BarChart(), title,
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
    return drawSection(new LineChart(), "Pokémon caught per save", saveNumbersOf(saves),
        caughtCounts);
  }

  ReportSection badgesOverTime(List<GameSave> saves) {
    List<Double> badgeCounts = saves.stream().map(save -> (double) save.getBadgeCount()).toList();
    return drawSection(new LineChart(), "Badges per save", saveNumbersOf(saves), badgeCounts);
  }

  private ReportSection drawBarSection(String title, Map<String, Double> valuesByLabel) {
    return drawSection(new BarChart(), title,
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
}
