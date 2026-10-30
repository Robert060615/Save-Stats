package se.lnu.rm222xi.savestats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ReportSection")
class ReportSectionTest {

  @Test
  @DisplayName("should have no notes when created with only a heading and a chart")
  void hasNoNotesByDefault() {
    assertTrue(new ReportSection("Heading", "<svg></svg>").notes().isEmpty());
  }

  @Test
  @DisplayName("should keep its notes when the list it was given changes afterwards")
  void copiesNotes() {
    List<String> notes = new ArrayList<>(List.of("First"));
    ReportSection section = new ReportSection("Heading", "<svg></svg>", notes);

    notes.add("Second");

    assertEquals(List.of("First"), section.notes());
  }
}
