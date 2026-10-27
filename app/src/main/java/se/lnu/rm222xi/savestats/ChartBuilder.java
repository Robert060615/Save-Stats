package se.lnu.rm222xi.savestats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import se.lnu.rm222xi.svgcharts.BarChart;
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

  private ReportSection drawBarSection(String title, Map<String, Double> valuesByLabel) {
    return drawSection(new BarChart(), title,
        new ArrayList<>(valuesByLabel.keySet()), new ArrayList<>(valuesByLabel.values()));
  }

  private ReportSection drawSection(SvgChart chart, String title, List<String> labels,
      List<Double> values) {
    chart.setTitle(title);
    chart.setLabels(labels);
    chart.setValues(values);
    return new ReportSection(title, chart.toSvg());
  }
}
