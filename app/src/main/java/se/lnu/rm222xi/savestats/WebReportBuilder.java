package se.lnu.rm222xi.savestats;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the whole report in one go from the choices on the start page. Each call gets its own
 * {@link ChartBuilder} and {@link Report}, so requests running at the same time never share
 * anything.
 */
class WebReportBuilder {

  private final SaveLoader loader;

  WebReportBuilder(SaveLoader loader) {
    this.loader = loader;
  }

  /**
   * Loads the saves and draws the charts the user asked for. The charts for each save come in the
   * order the saves were chosen, followed by the progress between saves.
   *
   * @param request the user's files and choices
   * @return the finished report
   * @throws SaveLoadException if one of the files is not a valid save
   * @throws se.lnu.rm222xi.svgcharts.ChartDataException if the color is not a hex color
   */
  Report build(ReportRequest request) throws SaveLoadException {
    ChartBuilder chartBuilder = new ChartBuilder();
    chartBuilder.setColor(request.getColor());
    List<GameSave> saves = loadAll(request.getSaves());

    Report report = new Report();
    for (GameSave save : saves) {
      for (ReportSection section : chartsFor(save, request, chartBuilder)) {
        report.add(withFileName(save, section));
      }
    }
    if (request.wants(ChartChoice.PROGRESS)) {
      report.add(chartBuilder.caughtOverTime(saves));
      report.add(chartBuilder.badgesOverTime(saves));
    }

    return report;
  }

  private List<GameSave> loadAll(List<UploadedSave> uploads) throws SaveLoadException {
    List<GameSave> saves = new ArrayList<>();
    for (UploadedSave upload : uploads) {
      saves.add(loader.load(upload.fileName(), upload.data()));
    }

    return saves;
  }

  /**
   * Draws the charts that belong to one save. Party charts are left out when the party is empty,
   * since there is nothing to draw.
   */
  private List<ReportSection> chartsFor(GameSave save, ReportRequest request,
      ChartBuilder chartBuilder) {
    List<ReportSection> sections = new ArrayList<>();
    if (request.wants(ChartChoice.PARTY_LEVELS) && save.hasParty()) {
      sections.add(chartBuilder.partyLevels(save));
    }
    if (request.wants(ChartChoice.POKEDEX)) {
      sections.add(chartBuilder.pokedexProgress(save));
    }
    if (request.wants(ChartChoice.TEAM)) {
      for (int i = 0; i < save.getPartyNicknames().size(); i++) {
        sections.add(chartBuilder.stats(save, i));
        sections.add(chartBuilder.ivs(save, i));
      }
    }

    return sections;
  }

  /** Puts the file name in the heading, so charts from different saves can be told apart. */
  private ReportSection withFileName(GameSave save, ReportSection section) {
    return new ReportSection(save.getFileName() + ": " + section.heading(), section.svg());
  }
}
