package se.lnu.rm222xi.savestats;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What the user asked for on the start page: the save files in the order they were chosen, the
 * charts to draw and their color.
 */
final class ReportRequest {

  private static final int MIN_SAVES_TO_COMPARE = 2;
  private final List<UploadedSave> saves;
  private final Set<ChartChoice> charts;
  private final String color;

  private ReportRequest(List<UploadedSave> saves, Set<ChartChoice> charts, String color) {
    this.saves = saves;
    this.charts = charts;
    this.color = color;
  }

  /**
   * Reads the form data that the start page sends.
   *
   * @param formData the request body, encoded as {@code application/x-www-form-urlencoded}
   * @return the user's choices
   * @throws InvalidRequestException if a file, chart or color is missing, a file cannot be
   *     decoded, or a chart needs more files than were sent
   */
  static ReportRequest fromFormData(String formData) throws InvalidRequestException {
    Map<String, List<String>> fields = decodeFields(formData);
    List<UploadedSave> saves = readSaves(fields);
    Set<ChartChoice> charts = readCharts(fields);
    String color = readColor(fields);

    if (charts.contains(ChartChoice.PROGRESS) && saves.size() < MIN_SAVES_TO_COMPARE) {
      throw new InvalidRequestException("Choose at least two save files to compare them.");
    }

    return new ReportRequest(saves, charts, color);
  }

  List<UploadedSave> getSaves() {
    return saves;
  }

  boolean wants(ChartChoice chart) {
    return charts.contains(chart);
  }

  String getColor() {
    return color;
  }

  /**
   * Splits form data like {@code a=1&b=2&a=3} into its fields. Fields with the same name keep the
   * order they were sent in.
   */
  private static Map<String, List<String>> decodeFields(String formData)
      throws InvalidRequestException {
    Map<String, List<String>> fields = new LinkedHashMap<>();
    for (String pair : formData.split("&")) {
      if (pair.isEmpty()) {
        continue;
      }

      String[] nameAndValue = pair.split("=", 2);
      String name = decode(nameAndValue[0]);
      String value = nameAndValue.length == 2 ? decode(nameAndValue[1]) : "";
      fields.computeIfAbsent(name, key -> new ArrayList<>()).add(value);
    }

    return fields;
  }

  private static String decode(String text) throws InvalidRequestException {
    try {
      return URLDecoder.decode(text, StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      throw new InvalidRequestException("The form data could not be read.");
    }
  }

  private static List<UploadedSave> readSaves(Map<String, List<String>> fields)
      throws InvalidRequestException {
    List<String> fileNames = fields.getOrDefault("fileName", List.of());
    List<String> contents = fields.getOrDefault("save", List.of());
    if (fileNames.isEmpty()) {
      throw new InvalidRequestException("Choose at least one save file.");
    }
    if (fileNames.size() != contents.size()) {
      throw new InvalidRequestException("Every save file needs both a name and contents.");
    }

    List<UploadedSave> saves = new ArrayList<>();
    for (int i = 0; i < fileNames.size(); i++) {
      String fileName = fileNames.get(i);
      saves.add(new UploadedSave(fileName, decodeBase64(fileName, contents.get(i))));
    }

    return saves;
  }

  private static byte[] decodeBase64(String fileName, String base64)
      throws InvalidRequestException {
    try {
      return Base64.getDecoder().decode(base64);
    } catch (IllegalArgumentException e) {
      throw new InvalidRequestException("Could not read the contents of " + fileName + ".");
    }
  }

  private static Set<ChartChoice> readCharts(Map<String, List<String>> fields)
      throws InvalidRequestException {
    Set<ChartChoice> charts = EnumSet.noneOf(ChartChoice.class);
    for (String value : fields.getOrDefault("chart", List.of())) {
      charts.add(ChartChoice.fromFormValue(value));
    }
    if (charts.isEmpty()) {
      throw new InvalidRequestException("Choose at least one chart.");
    }

    return charts;
  }

  private static String readColor(Map<String, List<String>> fields)
      throws InvalidRequestException {
    List<String> colors = fields.getOrDefault("color", List.of());
    if (colors.isEmpty()) {
      throw new InvalidRequestException("Choose a color.");
    }

    return colors.getFirst();
  }
}
