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
    return toPage("");
  }

  /**
   * Builds the same page as {@link #toHtml()} with a link at the top and bottom, for a report shown
   * by the web server rather than saved as a file.
   *
   * @param address where the link leads, for example {@code "/"}
   * @return the HTML document with the links
   */
  String toHtmlWithLinkTo(String address) {
    return toPage("<p><a href=\"" + address + "\">&larr; Back to the start page</a></p>\n");
  }

  private String toPage(String link) {
    StringBuilder html = new StringBuilder()
        .append("<!DOCTYPE html>\n")
        .append("<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n")
        .append("<title>Save Stats report</title>\n")
        .append("<style>body{font-family:sans-serif;max-width:640px;margin:2em auto;}")
        .append("svg{max-width:100%;height:auto;}</style>\n")
        .append("</head>\n<body>\n").append(link).append("<h1>Save Stats report</h1>\n");

    for (ReportSection section : sections) {
      html.append("<section>\n<h2>").append(Html.escape(section.heading())).append("</h2>\n");
      appendNotes(html, section.notes());
      html.append(section.svg()).append("\n</section>\n");
    }

    return html.append(link).append("</body>\n</html>\n").toString();
  }

  private void appendNotes(StringBuilder html, List<String> notes) {
    if (notes.isEmpty()) {
      return;
    }

    html.append("<ul>\n");
    for (String note : notes) {
      html.append("<li>").append(Html.escape(note)).append("</li>\n");
    }
    html.append("</ul>\n");
  }
}
