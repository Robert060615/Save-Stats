package se.lnu.rm222xi.savestats;

import java.util.List;
import se.lnu.rm222xi.svgcharts.BarChart;
import se.lnu.rm222xi.svgcharts.SvgChart;

/**
 * Turns save data into report sections with charts drawn by the svg-charts module.
 */
class ChartBuilder {

  ReportSection partyLevels(GameSave save) {
    String title = "Party levels – " + save.getTrainerName();
    return drawSection(new BarChart(), title, save.getPartyNicknames(), save.getPartyLevels());
  }

  private ReportSection drawSection(SvgChart chart, String title, List<String> labels,
      List<Double> values) {
    chart.setTitle(title);
    chart.setLabels(labels);
    chart.setValues(values);
    return new ReportSection(title, chart.toSvg());
  }
}
