package se.lnu.rm222xi.savestats;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ReportRequest")
class ReportRequestTest {

  /** Bytes that Base64 encodes with "+", "/" and "=", which must survive the URL encoding. */
  private static final byte[] FIRST = {(byte) 0xFB, (byte) 0xFF, 0x00, 0x01};
  private static final byte[] SECOND = {0x10, 0x20};
  private static final String BLACK = "#000000";

  @Nested
  @DisplayName("with valid form data")
  class ValidTest {

    @Test
    @DisplayName("should keep the files in the order they were sent, with names and contents")
    void readsSavesInOrder() throws InvalidRequestException {
      ReportRequest request = ReportRequest.fromFormData(new FormData()
          .chart("pokedex").color(BLACK)
          .save("second.srm", SECOND).save("first.sav", FIRST).toString());

      List<UploadedSave> saves = request.getSaves();

      assertEquals(2, saves.size());
      assertEquals("second.srm", saves.get(0).fileName());
      assertArrayEquals(SECOND, saves.get(0).data());
      assertEquals("first.sav", saves.get(1).fileName());
      assertArrayEquals(FIRST, saves.get(1).data());
    }

    @Test
    @DisplayName("should decode file names with spaces, plus signs and non-English letters")
    void decodesFileNames() throws InvalidRequestException {
      ReportRequest request = ReportRequest.fromFormData(new FormData()
          .chart("pokedex").color(BLACK).save("min fil+å.sav", FIRST).toString());

      assertEquals("min fil+å.sav", request.getSaves().getFirst().fileName());
    }

    @Test
    @DisplayName("should want exactly the charts that were ticked")
    void readsCharts() throws InvalidRequestException {
      ReportRequest request = ReportRequest.fromFormData(new FormData()
          .chart("partyLevels").chart("team").color(BLACK).save("a.sav", FIRST).toString());

      assertTrue(request.wants(ChartChoice.PARTY_LEVELS));
      assertTrue(request.wants(ChartChoice.TEAM));
      assertFalse(request.wants(ChartChoice.POKEDEX));
      assertFalse(request.wants(ChartChoice.PROGRESS));
    }

    @Test
    @DisplayName("should read every chart value the start page can send")
    void readsAllChartValues() throws InvalidRequestException {
      ReportRequest request = ReportRequest.fromFormData(new FormData()
          .chart("partyLevels").chart("pokedex").chart("team").chart("progress")
          .color(BLACK).save("a.sav", FIRST).save("b.sav", SECOND).toString());

      for (ChartChoice chart : ChartChoice.values()) {
        assertTrue(request.wants(chart), chart.name());
      }
    }

    @Test
    @DisplayName("should read the color with its # sign")
    void readsColor() throws InvalidRequestException {
      ReportRequest request = ReportRequest.fromFormData(new FormData()
          .chart("pokedex").color("#e4572e").save("a.sav", FIRST).toString());

      assertEquals("#e4572e", request.getColor());
    }

    @Test
    @DisplayName("should ignore empty fields between the & signs")
    void ignoresEmptyFields() throws InvalidRequestException {
      ReportRequest request = ReportRequest.fromFormData("&" + new FormData()
          .chart("pokedex").raw("").color(BLACK).save("a.sav", FIRST) + "&");

      assertEquals(1, request.getSaves().size());
      assertEquals(BLACK, request.getColor());
    }

    @Test
    @DisplayName("should keep = signs in a value that were not URL encoded")
    void keepsUnencodedEqualsSigns() throws InvalidRequestException {
      ReportRequest request = ReportRequest.fromFormData(new FormData()
          .chart("pokedex").color(BLACK).raw("fileName=x=y.sav").field("save", "EA==").toString());

      assertEquals("x=y.sav", request.getSaves().getFirst().fileName());
    }

    @Test
    @DisplayName("should allow progress between saves with two files")
    void allowsProgressWithTwoSaves() throws InvalidRequestException {
      ReportRequest request = ReportRequest.fromFormData(new FormData()
          .chart("progress").color(BLACK).save("a.sav", FIRST).save("b.sav", SECOND).toString());

      assertTrue(request.wants(ChartChoice.PROGRESS));
    }
  }

  @Nested
  @DisplayName("with missing or broken form data")
  class InvalidTest {

    @Test
    @DisplayName("should ask for a file when none was sent")
    void throwsWithoutSaves() {
      assertRejected("Choose at least one save file.",
          new FormData().chart("pokedex").color(BLACK));
    }

    @Test
    @DisplayName("should ask for a file when the request is empty")
    void throwsForEmptyRequest() {
      assertRejected("Choose at least one save file.", new FormData());
    }

    @Test
    @DisplayName("should refuse a file name without contents")
    void throwsForNameWithoutContents() {
      assertRejected("Every save file needs both a name and contents.",
          new FormData().chart("pokedex").color(BLACK).field("fileName", "a.sav"));
    }

    @Test
    @DisplayName("should name the file whose contents are not Base64")
    void throwsForInvalidBase64() {
      assertRejected("Could not read the contents of a.sav.", new FormData()
          .chart("pokedex").color(BLACK).field("fileName", "a.sav").field("save", "!!!!"));
    }

    @Test
    @DisplayName("should refuse form data with a broken % escape")
    void throwsForBrokenEscape() {
      assertRejected("The form data could not be read.", new FormData()
          .chart("pokedex").color(BLACK).field("fileName", "a.sav").raw("save=%%"));
    }

    @Test
    @DisplayName("should ask for a chart when none was ticked")
    void throwsWithoutCharts() {
      assertRejected("Choose at least one chart.",
          new FormData().color(BLACK).save("a.sav", FIRST));
    }

    @Test
    @DisplayName("should name a chart value it does not know")
    void throwsForUnknownChart() {
      assertRejected("Unknown chart: everything",
          new FormData().chart("everything").color(BLACK).save("a.sav", FIRST));
    }

    @Test
    @DisplayName("should ask for a color when none was sent")
    void throwsWithoutColor() {
      assertRejected("Choose a color.", new FormData().chart("pokedex").save("a.sav", FIRST));
    }

    @Test
    @DisplayName("should ask for two files when progress between saves is ticked with one")
    void throwsForProgressWithOneSave() {
      assertRejected("Choose at least two save files to compare them.",
          new FormData().chart("progress").color(BLACK).save("a.sav", FIRST));
    }

    private void assertRejected(String expectedMessage, FormData formData) {
      InvalidRequestException exception = assertThrows(InvalidRequestException.class,
          () -> ReportRequest.fromFormData(formData.toString()));

      assertEquals(expectedMessage, exception.getMessage());
    }
  }
}
