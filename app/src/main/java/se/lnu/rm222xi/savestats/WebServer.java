package se.lnu.rm222xi.savestats;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Serves Save Stats to a web browser on this computer. It only listens on localhost, so nobody
 * else on the network can reach it.
 */
class WebServer {

  /** Lets a few requests run at the same time instead of one after another. */
  private static final int THREADS = 4;
  private static final int HTTP_OK = 200;
  private static final String START_PAGE_FILE = "start-page.html";
  private final HttpServer server;
  private final String startPage;

  /**
   * Creates the server without starting it.
   *
   * @param port the port to listen on
   * @throws IOException if the port is already in use or cannot be opened, or if the start page
   *     is missing from the jar
   */
  WebServer(int port) throws IOException {
    startPage = readResource(START_PAGE_FILE);
    InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
    server = HttpServer.create(address, 0);
    server.createContext("/", this::showStartPage);
    server.setExecutor(Executors.newFixedThreadPool(THREADS));
  }

  void start() {
    server.start();
  }

  /**
   * Gives the address to open in the browser.
   *
   * @return the address, for example {@code http://localhost:8080}
   */
  String address() {
    return "http://localhost:" + server.getAddress().getPort();
  }

  private void showStartPage(HttpExchange exchange) throws IOException {
    sendHtml(exchange, HTTP_OK, startPage);
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
