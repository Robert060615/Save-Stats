package se.lnu.rm222xi.savestats;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import se.lnu.rm222xi.svgcharts.ChartDataException;

/**
 * Serves Save Stats to a web browser on this computer. It only listens on localhost, so nobody
 * else on the network can reach it. Requests are handled one at a time, which is plenty for one
 * user.
 */
class WebServer {

  private static final int HTTP_OK = 200;
  private static final int HTTP_BAD_REQUEST = 400;
  private static final int HTTP_PAYLOAD_TOO_LARGE = 413;
  /** Room for ten saves: one is about 131 kB once it is Base64 and URL encoded. */
  private static final int MAX_REQUEST_BYTES = 1_500_000;
  private static final String START_PAGE_FILE = "start-page.html";
  private final HttpServer server;
  private final String startPage;
  private final WebReportBuilder reportBuilder;

  /**
   * Creates the server without starting it.
   *
   * @param port the port to listen on
   * @param reportBuilder builds the report for each request
   * @throws IOException if the port is already in use or cannot be opened, or if the start page
   *     is missing from the jar
   */
  WebServer(int port, WebReportBuilder reportBuilder) throws IOException {
    this.reportBuilder = reportBuilder;
    startPage = readResource(START_PAGE_FILE);
    InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
    server = HttpServer.create(address, 0);
    server.createContext("/", this::showStartPage);
    server.createContext("/report", this::showReport);
  }

  void start() {
    server.start();
  }

  private void showStartPage(HttpExchange exchange) throws IOException {
    sendHtml(exchange, HTTP_OK, startPage);
  }

  /**
   * Builds the report from the form on the start page. Reads one byte more than the limit, to
   * notice a request that is too large without reading all of it into memory.
   */
  private void showReport(HttpExchange exchange) throws IOException {
    byte[] body = exchange.getRequestBody().readNBytes(MAX_REQUEST_BYTES + 1);
    if (body.length > MAX_REQUEST_BYTES) {
      showError(exchange, HTTP_PAYLOAD_TOO_LARGE,
          "The files are too large. A save file is 32 kB, and at most ten fit at a time.");
      return;
    }

    try {
      String formData = new String(body, StandardCharsets.UTF_8);
      Report report = reportBuilder.build(ReportRequest.fromFormData(formData));
      sendHtml(exchange, HTTP_OK, report.toHtmlWithLinkTo("/"));
    } catch (InvalidRequestException | SaveLoadException | ChartDataException e) {
      showError(exchange, HTTP_BAD_REQUEST, e.getMessage());
    }
  }

  private void showError(HttpExchange exchange, int status, String message) throws IOException {
    sendHtml(exchange, status, "<!DOCTYPE html>\n"
        + "<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n"
        + "<title>Save Stats - error</title>\n"
        + "<style>body{font-family:sans-serif;max-width:640px;margin:2em auto;}</style>\n"
        + "</head>\n<body>\n<h1>The charts could not be shown</h1>\n"
        + "<p>" + Html.escape(message) + "</p>\n"
        + "<p><a href=\"/\">Back to the start page</a></p>\n"
        + "</body>\n</html>\n");
  }

  /**
   * Reads a text file that is packaged next to this class in the jar.
   *
   * @param name the file name, relative to this package
   * @return the contents of the file
   * @throws IOException if the file is missing or cannot be read
   */
  private static String readResource(String name) throws IOException {
    try (InputStream input = WebServer.class.getResourceAsStream(name)) {
      if (input == null) {
        throw new IOException("Missing resource: " + name);
      }

      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
    byte[] body = html.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
    exchange.sendResponseHeaders(status, body.length);
    try (OutputStream output = exchange.getResponseBody()) {
      output.write(body);
    }
  }
}
