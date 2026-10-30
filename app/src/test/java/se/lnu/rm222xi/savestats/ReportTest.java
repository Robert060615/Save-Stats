package se.lnu.rm222xi.savestats;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Report")
class ReportTest {

  private final Report report = new Report();

  @Test
  @DisplayName("should be empty when nothing is added")
  void isEmptyAtStart() {
    assertTrue(report.isEmpty());
  }

  @Test
  @DisplayName("should not be empty after a section is added")
  void isNotEmptyAfterAdd() {
    report.add(new ReportSection("Heading", "<svg></svg>"));

    assertFalse(report.isEmpty());
  }

  @Test
  @DisplayName("should contain each heading and chart in the HTML")
  void containsHeadingAndChart() {
    report.add(new ReportSection("Party levels", "<svg id=\"chart\"></svg>"));

    String html = report.toHtml();

    assertTrue(html.contains("<h2>Party levels</h2>"));
    assertTrue(html.contains("<svg id=\"chart\"></svg>"));
  }

  @Test
  @DisplayName("should keep the sections in the order they were added")
  void keepsOrder() {
    report.add(new ReportSection("First", "<svg></svg>"));
    report.add(new ReportSection("Second", "<svg></svg>"));

    String html = report.toHtml();

    assertTrue(html.indexOf("First") < html.indexOf("Second"));
  }

  @Test
  @DisplayName("should escape characters in headings that would break the HTML")
  void escapesHeadings() {
    report.add(new ReportSection("<b> & co", "<svg></svg>"));

    String html = report.toHtml();

    assertTrue(html.contains("<h2>&lt;b&gt; &amp; co</h2>"));
  }

  @Test
  @DisplayName("should have no links when saved as a file")
  void hasNoLinkAsFile() {
    report.add(new ReportSection("Heading", "<svg></svg>"));

    assertFalse(report.toHtml().contains("<a "));
  }

  @Test
  @DisplayName("should link to the given address above and below the charts")
  void linksAboveAndBelowCharts() {
    report.add(new ReportSection("Heading", "<svg></svg>"));

    String html = report.toHtmlWithLinkTo("/");

    String link = "<a href=\"/\">&larr; Back to the start page</a>";
    int chart = html.indexOf("<svg>");
    assertTrue(html.indexOf(link) < chart);
    assertTrue(html.lastIndexOf(link) > chart);
  }

  @Test
  @DisplayName("should show the same charts with or without the link")
  void sameChartsWithLink() {
    report.add(new ReportSection("Heading", "<svg id=\"chart\"></svg>"));

    String html = report.toHtmlWithLinkTo("/");

    assertTrue(html.contains("<h2>Heading</h2>"));
    assertTrue(html.contains("<svg id=\"chart\"></svg>"));
  }
}
