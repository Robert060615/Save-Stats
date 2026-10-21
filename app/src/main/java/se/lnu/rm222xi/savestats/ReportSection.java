package se.lnu.rm222xi.savestats;

/**
 * One chart in the report, with the heading shown above it.
 *
 * @param heading the heading, as plain text
 * @param svg the chart as SVG markup
 */
record ReportSection(String heading, String svg) {
}
