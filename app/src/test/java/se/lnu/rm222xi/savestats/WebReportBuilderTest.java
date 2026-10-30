package se.lnu.rm222xi.savestats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.lnu.rm222xi.svgcharts.ChartDataException;
import se.lnu.savechecker.InvalidSaveFileException;

/**
 * Checks which charts end up in the report and in what order, by reading the headings back out of
 * the HTML. What each chart shows is already covered by {@link ChartBuilderTest}.
 */
@DisplayName("WebReportBuilder")
class WebReportBuilderTest {

  /** The sample saves in the repository root. Tests run from the app folder. */
  private static final Path SAMPLES = Path.of("../samples");
  private static final Pattern HEADING = Pattern.compile("<h2>(.*?)</h2>");
  private static final String DEFAULT_COLOR = "#1f77b4";
  private static final String ORANGE = "#e4572e";

  /** Where the party size is stored, and the range the checksum covers. */
  private static final int PARTY_COUNT_OFFSET = 0x2F2C;
  private static final int CHECKSUM_START = 0x2598;
  private static final int CHECKSUM_END = 0x3522;

  /** Trainer A, party EXEGGUTOR, PSYDUCK, WEEDLE, VENOMOTH, STARYU. */
  private static byte[] yellow;

  /** Trainer YELLOW, party METAPOD. */
  private static byte[] otherYellow;

  private final WebReportBuilder builder = new WebReportBuilder(new SaveLoader());

  @BeforeAll
  static void readSamples() throws IOException {
    yellow = Files.readAllBytes(SAMPLES.resolve("yellow-2.srm"));
    otherYellow = Files.readAllBytes(SAMPLES.resolve("yellow-3.srm"));
  }

  @Test
  @DisplayName("should draw party levels for every save, in the order they were chosen")
  void partyLevelsForEachSave() throws Exception {
    Report report = build(new FormData().chart("partyLevels").color(ORANGE)
        .save("yellow-3.srm", otherYellow).save("yellow-2.srm", yellow));

    assertEquals(List.of("yellow-3.srm: Party levels - YELLOW", "yellow-2.srm: Party levels - A"),
        headingsOf(report));
  }

  @Test
  @DisplayName("should draw stats and then IVs for each Pokémon in the party")
  void statsAndIvsForWholeParty() throws Exception {
    Report report = build(new FormData().chart("team").color(ORANGE).save("y.srm", yellow));

    List<String> headings = headingsOf(report);

    assertEquals(10, headings.size());
    assertEquals("y.srm: Stats of EXEGGUTOR", headings.get(0));
    assertEquals("y.srm: IVs of EXEGGUTOR (0-15)", headings.get(1));
    assertEquals("y.srm: Stats of STARYU", headings.get(8));
    assertEquals("y.srm: IVs of STARYU (0-15)", headings.get(9));
  }

  @Test
  @DisplayName("should group the charts by save: party levels, Pokédex, then the party")
  void groupsChartsBySave() throws Exception {
    Report report = build(new FormData().chart("team").chart("pokedex").chart("partyLevels")
        .color(ORANGE).save("a.srm", otherYellow).save("b.srm", otherYellow));

    assertEquals(List.of(
        "a.srm: Party levels - YELLOW", "a.srm: Pokédex - 1 of 151 caught",
        "a.srm: Stats of METAPOD", "a.srm: IVs of METAPOD (0-15)",
        "b.srm: Party levels - YELLOW", "b.srm: Pokédex - 1 of 151 caught",
        "b.srm: Stats of METAPOD", "b.srm: IVs of METAPOD (0-15)"),
        headingsOf(report));
  }

  @Test
  @DisplayName("should put progress between saves last, without a file name")
  void progressLast() throws Exception {
    Report report = build(new FormData().chart("progress").chart("pokedex").color(ORANGE)
        .save("a.srm", yellow).save("b.srm", otherYellow));

    assertEquals(List.of("a.srm: Pokédex - 5 of 151 caught", "b.srm: Pokédex - 1 of 151 caught",
        "Pokémon caught per save", "Badges per save"), headingsOf(report));
  }

  @Test
  @DisplayName("should list the files under the heading of the first progress chart")
  void listsSavesAboveProgressCharts() throws Exception {
    Report report = build(new FormData().chart("progress").color(ORANGE)
        .save("a.srm", yellow).save("b.srm", otherYellow));

    String html = report.toHtml();

    assertTrue(html.contains("<h2>Pokémon caught per save</h2>\n<ul>\n"
        + "<li>Save 1: a.srm (trainer A)</li>\n<li>Save 2: b.srm (trainer YELLOW)</li>\n</ul>"));
  }

  @Test
  @DisplayName("should leave out party charts for an empty party but keep the Pokédex")
  void skipsEmptyParty() throws Exception {
    Report report = build(new FormData().chart("partyLevels").chart("team").chart("pokedex")
        .color(ORANGE).save("empty.srm", withEmptyParty(otherYellow)));

    assertEquals(List.of("empty.srm: Pokédex - 1 of 151 caught"), headingsOf(report));
  }

  @Test
  @DisplayName("should draw every chart in the chosen color")
  void usesChosenColor() throws Exception {
    Report report = build(new FormData().chart("pokedex").chart("progress").color(ORANGE)
        .save("a.srm", yellow).save("b.srm", otherYellow));

    String html = report.toHtml();

    assertTrue(html.contains(ORANGE));
    assertFalse(html.contains(DEFAULT_COLOR));
  }

  @Test
  @DisplayName("should refuse a color that is not a hex color")
  void throwsForInvalidColor() throws InvalidRequestException {
    ReportRequest request = ReportRequest.fromFormData(
        new FormData().chart("pokedex").color("orange").save("a.srm", yellow).toString());

    assertThrows(ChartDataException.class, () -> builder.build(request));
  }

  @Test
  @DisplayName("should name the file that is not a valid save")
  void throwsForInvalidSave() throws InvalidRequestException {
    ReportRequest request = ReportRequest.fromFormData(new FormData().chart("pokedex")
        .color(ORANGE).save("a.srm", yellow).save("notes.txt", new byte[] {1, 2, 3}).toString());

    SaveLoadException exception = assertThrows(SaveLoadException.class,
        () -> builder.build(request));

    assertTrue(exception.getMessage().startsWith("Not a valid save file: notes.txt: "));
    assertInstanceOf(InvalidSaveFileException.class, exception.getCause());
  }

  private Report build(FormData formData) throws InvalidRequestException, SaveLoadException {
    return builder.build(ReportRequest.fromFormData(formData.toString()));
  }

  private static List<String> headingsOf(Report report) {
    Matcher matcher = HEADING.matcher(report.toHtml());
    return matcher.results().map(result -> result.group(1)).toList();
  }

  /**
   * Copies a save and sets its party size to zero. The game checks a checksum over part of the
   * file, so it is computed again the same way: the inverted sum of the bytes, cut to 8 bits.
   */
  private static byte[] withEmptyParty(byte[] save) {
    byte[] copy = save.clone();
    copy[PARTY_COUNT_OFFSET] = 0;

    int sum = 0;
    for (int i = CHECKSUM_START; i <= CHECKSUM_END; i++) {
      sum += copy[i] & 0xFF;
    }
    copy[CHECKSUM_END + 1] = (byte) ~sum;

    return copy;
  }
}
