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
    send(exchange, HTTP_OK, "text/html", startPage);
  }

  private void showReport(HttpExchange exchange) throws IOException {
    String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    try {
      Report report = reportBuilder.build(ReportRequest.fromFormData(formData));
      send(exchange, HTTP_OK, "text/html", report.toHtml());
    } catch (InvalidRequestException | SaveLoadException | ChartDataException e) {
      send(exchange, HTTP_BAD_REQUEST, "text/plain", e.getMessage());
    }
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

  private void send(HttpExchange exchange, int status, String contentType, String text)
      throws IOException {
    byte[] body = text.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
    exchange.sendResponseHeaders(status, body.length);
    try (OutputStream output = exchange.getResponseBody()) {
      output.write(body);
    }
  }
}
