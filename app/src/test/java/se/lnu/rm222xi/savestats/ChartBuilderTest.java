package se.lnu.rm222xi.savestats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.lnu.rm222xi.svgcharts.BarChart;
import se.lnu.rm222xi.svgcharts.ChartDataException;
import se.lnu.rm222xi.svgcharts.LineChart;
import se.lnu.rm222xi.svgcharts.SvgChart;

/**
 * Compares the charts with charts drawn directly with svg-charts from values read off the sample
 * saves, so a wrong value, order or stat is caught and not only a wrong heading.
 */
@DisplayName("ChartBuilder")
class ChartBuilderTest {

  /** The sample saves in the repository root. Tests run from the app folder. */
  private static final Path SAMPLES = Path.of("../samples");
  private static final List<String> STAT_NAMES =
      List.of("HP", "Attack", "Defense", "Speed", "Special");
  private static final String DEFAULT_COLOR = "#1f77b4";
  private static final String ORANGE = "#e4572e";

  /** Trainer A, 3 badges, 5 caught, 76 seen, party EXEGGUTOR, PSYDUCK, WEEDLE, VENOMOTH, STARYU. */
  private static GameSave yellow;

  /** Trainer YELLOW, 0 badges, 1 caught, party METAPOD with 18 of 22 HP left. */
  private static GameSave otherYellow;

  private final ChartBuilder chartBuilder = new ChartBuilder();

  @BeforeAll
  static void loadSamples() throws SaveLoadException {
    SaveLoader loader = new SaveLoader();
    yellow = loader.load(SAMPLES.resolve("yellow-2.srm"));
    otherYellow = loader.load(SAMPLES.resolve("yellow-3.srm"));
  }

  @Nested
  @DisplayName("charts of one save")
  class OneSaveTest {

    @Test
    @DisplayName("party levels should name the trainer and show every party Pokémon")
    void partyLevels() {
      ReportSection section = chartBuilder.partyLevels(yellow);

      assertEquals("Party levels - A", section.heading());
      for (String nickname : yellow.getPartyNicknames()) {
        assertTrue(section.svg().contains(">" + nickname + "<"));
      }
    }

    @Test
    @DisplayName("stats should show max HP, Attack, Defense, Speed and Special in that order")
    void stats() {
      ReportSection section = chartBuilder.stats(otherYellow, 0);

      String title = "Stats of METAPOD";
      assertEquals(title, section.heading());
      assertEquals(draw(new BarChart(), title, STAT_NAMES, List.of(22.0, 8.0, 13.0, 9.0, 9.0)),
          section.svg());
    }

    @Test
    @DisplayName("IVs should show the five IVs in the same order as the stats")
    void ivs() {
      ReportSection section = chartBuilder.ivs(otherYellow, 0);

      String title = "IVs of METAPOD (0-15)";
      assertEquals(title, section.heading());
      assertEquals(draw(new BarChart(), title, STAT_NAMES, List.of(1.0, 6.0, 12.0, 8.0, 13.0)),
          section.svg());
    }

    @Test
    @DisplayName("Pokédex progress should split the 151 Pokémon into caught, seen and not seen")
    void pokedexProgress() {
      ReportSection section = chartBuilder.pokedexProgress(yellow);

      String title = "Pokédex - 5 of 151 caught";
      assertEquals(title, section.heading());
      assertEquals(draw(new BarChart(), title, List.of("Caught", "Seen, not caught", "Not seen"),
          List.of(5.0, 71.0, 75.0)), section.svg());
    }
  }

  @Nested
  @DisplayName("charts of several saves")
  class SeveralSavesTest {

    private final List<GameSave> saves = List.of(yellow, otherYellow);
    private final List<String> saveLabels = List.of("Save 1", "Save 2");

    @Test
    @DisplayName("should draw the number caught in each save, in the order given")
    void caughtOverTime() {
      ReportSection section = chartBuilder.caughtOverTime(saves);

      String title = "Pokémon caught per save";
      assertEquals(title, section.heading());
      assertEquals(draw(new LineChart(), title, saveLabels, List.of(5.0, 1.0)), section.svg());
    }

    @Test
    @DisplayName("should draw the number of badges in each save, in the order given")
    void badgesOverTime() {
      ReportSection section = chartBuilder.badgesOverTime(saves);

      String title = "Badges per save";
      assertEquals(title, section.heading());
      assertEquals(draw(new LineChart(), title, saveLabels, List.of(3.0, 0.0)), section.svg());
    }
  }

  @Nested
  @DisplayName("setColor()")
  class ColorTest {

    @Test
    @DisplayName("charts should be blue before a color is chosen")
    void usesDefaultColor() {
      assertTrue(chartBuilder.partyLevels(yellow).svg().contains(DEFAULT_COLOR));
    }

    @Test
    @DisplayName("bar and line charts built afterwards should get the chosen color")
    void usesChosenColor() {
      chartBuilder.setColor(ORANGE);

      String barSvg = chartBuilder.partyLevels(yellow).svg();
      String lineSvg = chartBuilder.caughtOverTime(List.of(yellow, otherYellow)).svg();

      assertTrue(barSvg.contains(ORANGE));
      assertTrue(lineSvg.contains(ORANGE));
      assertFalse(barSvg.contains(DEFAULT_COLOR));
    }

    @Test
    @DisplayName("charts built before should keep their color")
    void keepsColorOfEarlierCharts() {
      String earlierSvg = chartBuilder.partyLevels(yellow).svg();

      chartBuilder.setColor(ORANGE);

      assertFalse(earlierSvg.contains(ORANGE));
    }

    @Test
    @DisplayName("an invalid color should throw and leave the current color unchanged")
    void rejectsInvalidColor() {
      chartBuilder.setColor(ORANGE);

      assertThrows(ChartDataException.class, () -> chartBuilder.setColor("red"));
      assertTrue(chartBuilder.partyLevels(yellow).svg().contains(ORANGE));
    }
  }

  private String draw(SvgChart chart, String title, List<String> labels, List<Double> values) {
    chart.setTitle(title);
    chart.setLabels(labels);
    chart.setValues(values);
    return chart.toSvg();
  }
}
