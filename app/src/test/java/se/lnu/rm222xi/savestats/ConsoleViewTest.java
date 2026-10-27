package se.lnu.rm222xi.savestats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@DisplayName("ConsoleView")
class ConsoleViewTest {

  private static final int OPTION_COUNT = MenuOption.values().length;

  private final ByteArrayOutputStream output = new ByteArrayOutputStream();

  @Nested
  @DisplayName("chooseMenuOption()")
  class ChooseMenuOptionTest {

    @Test
    @DisplayName("should return the option with the number the user typed")
    void returnsChosenOption() {
      ConsoleView view = viewWithInput("1\n");

      assertEquals(MenuOption.values()[0], view.chooseMenuOption());
    }

    @Test
    @DisplayName("should accept the last number in the menu")
    void acceptsLastOption() {
      ConsoleView view = viewWithInput(OPTION_COUNT + "\n");

      assertEquals(MenuOption.values()[OPTION_COUNT - 1], view.chooseMenuOption());
    }

    @Test
    @DisplayName("should show every option with its number")
    void showsEveryOption() {
      ConsoleView view = viewWithInput("1\n");

      view.chooseMenuOption();

      MenuOption[] options = MenuOption.values();
      for (int i = 0; i < options.length; i++) {
        assertTrue(printed().contains((i + 1) + ") " + options[i].getDescription()));
      }
    }

    @Test
    @DisplayName("should ask again until the answer is a number in the menu")
    void asksAgainForInvalidAnswers() {
      ConsoleView view = viewWithInput("abc\n0\n" + (OPTION_COUNT + 1) + "\n\n2\n");

      MenuOption chosen = view.chooseMenuOption();

      assertEquals(MenuOption.values()[1], chosen);
      assertEquals(4, countOccurrences("Error: Please enter a number from 1 to " + OPTION_COUNT));
    }

    @Test
    @DisplayName("should treat a number too large for an int as invalid instead of crashing")
    void rejectsHugeNumber() {
      ConsoleView view = viewWithInput("12345678901\n1\n");

      MenuOption chosen = view.chooseMenuOption();

      assertEquals(MenuOption.values()[0], chosen);
      assertEquals(1, countOccurrences("Error: Please enter a number from 1 to " + OPTION_COUNT));
    }

    @Test
    @Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @DisplayName("should throw InputClosedException when there is nothing more to read")
    void throwsWhenInputIsClosed() {
      // Output is thrown away, so a bug that asks forever times out instead of filling the memory.
      ConsoleView view = new ConsoleView(new ByteArrayInputStream(new byte[0]),
          new PrintStream(OutputStream.nullOutputStream(), true, StandardCharsets.UTF_8));

      assertThrows(InputClosedException.class, view::chooseMenuOption);
    }
  }

  @Nested
  @DisplayName("chooseFrom()")
  class ChooseFromTest {

    @Test
    @DisplayName("should return the index of the chosen item, starting at 0")
    void returnsZeroBasedIndex() {
      ConsoleView view = viewWithInput("2\n");

      int index = view.chooseFrom("Which Pokémon?", List.of("PIKACHU", "EEVEE"));

      assertEquals(1, index);
    }

    @Test
    @DisplayName("should show the question and the numbered items")
    void showsQuestionAndItems() {
      ConsoleView view = viewWithInput("1\n");

      view.chooseFrom("Which Pokémon?", List.of("PIKACHU", "EEVEE"));

      assertTrue(printed().contains("Which Pokémon?"));
      assertTrue(printed().contains("1) PIKACHU"));
      assertTrue(printed().contains("2) EEVEE"));
    }
  }

  @Nested
  @DisplayName("ask()")
  class AskTest {

    @Test
    @DisplayName("should return the answer without surrounding spaces")
    void trimsAnswer() {
      ConsoleView view = viewWithInput("  #e4572e  \n");

      assertEquals("#e4572e", view.ask("Hex color:"));
    }
  }

  @Test
  @DisplayName("should start error messages with \"Error: \"")
  void prefixesErrors() {
    ConsoleView view = viewWithInput("");

    view.showError("Something went wrong");

    assertTrue(printed().contains("Error: Something went wrong"));
  }

  private ConsoleView viewWithInput(String typedText) {
    ByteArrayInputStream input =
        new ByteArrayInputStream(typedText.getBytes(StandardCharsets.UTF_8));
    return new ConsoleView(input, new PrintStream(output, true, StandardCharsets.UTF_8));
  }

  private String printed() {
    return output.toString(StandardCharsets.UTF_8);
  }

  private int countOccurrences(String text) {
    return printed().split(Pattern.quote(text), -1).length - 1;
  }
}
