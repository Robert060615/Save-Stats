package se.lnu.rm222xi.savestats;

import java.util.ArrayList;
import java.util.List;

/**
 * The HTML page the user gets in the end: a heading followed by the charts in the order they were
 * added.
 */
class Report {

  private final List<ReportSection> sections = new ArrayList<>();

  void add(ReportSection section) {
    sections.add(section);
  }

  boolean isEmpty() {
    return sections.isEmpty();
  }

  /**
   * Builds the complete HTML page.
   *
   * @return a standalone HTML document that can be opened in any web browser
   */
  String toHtml() {
    StringBuilder html = new StringBuilder()
        .append("<!DOCTYPE html>\n")
        .append("<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n")
        .append("<title>Save Stats report</title>\n")
        .append("<style>body{font-family:sans-serif;max-width:640px;margin:2em auto;}")
        .append("svg{max-width:100%;height:auto;}</style>\n")
        .append("</head>\n<body>\n<h1>Save Stats report</h1>\n");

    for (ReportSection section : sections) {
      html.append("<section>\n<h2>").append(escape(section.heading())).append("</h2>\n")
          .append(section.svg()).append("\n</section>\n");
    }

    return html.append("</body>\n</html>\n").toString();
  }

  private String escape(String text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
