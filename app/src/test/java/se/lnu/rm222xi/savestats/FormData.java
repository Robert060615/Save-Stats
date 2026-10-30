package se.lnu.rm222xi.savestats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.StringJoiner;

/**
 * Builds form data the same way the start page sends it, so tests do not have to encode it by
 * hand.
 */
final class FormData {

  private final StringJoiner fields = new StringJoiner("&");

  FormData chart(String formValue) {
    return field("chart", formValue);
  }

  FormData color(String color) {
    return field("color", color);
  }

  /** Adds a file name followed by its contents in Base64, like the start page's JavaScript. */
  FormData save(String fileName, byte[] data) {
    return field("fileName", fileName).field("save", Base64.getEncoder().encodeToString(data));
  }

  FormData field(String name, String value) {
    fields.add(encode(name) + "=" + encode(value));
    return this;
  }

  /** Adds text exactly as given, for data the start page would never send. */
  FormData raw(String text) {
    fields.add(text);
    return this;
  }

  @Override
  public String toString() {
    return fields.toString();
  }

  private static String encode(String text) {
    return URLEncoder.encode(text, StandardCharsets.UTF_8);
  }
}
