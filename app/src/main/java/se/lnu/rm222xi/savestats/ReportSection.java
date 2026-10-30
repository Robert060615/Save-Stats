package se.lnu.rm222xi.savestats;

import java.util.List;

/**
 * One chart in the report, with the heading shown above it and any notes between the two.
 *
 * @param heading the heading, as plain text
 * @param svg the chart as SVG markup
 * @param notes lines that explain the chart, as plain text, for example what its labels stand for
 */
record ReportSection(String heading, String svg, List<String> notes) {

  ReportSection {
    notes = List.copyOf(notes);
  }

  /**
   * Creates a section without notes.
   *
   * @param heading the heading, as plain text
   * @param svg the chart as SVG markup
   */
  ReportSection(String heading, String svg) {
    this(heading, svg, List.of());
  }
}
