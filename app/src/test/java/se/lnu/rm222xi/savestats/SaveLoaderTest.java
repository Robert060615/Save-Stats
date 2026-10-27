package se.lnu.rm222xi.savestats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.lnu.savechecker.InvalidSaveFileException;

@DisplayName("SaveLoader")
class SaveLoaderTest {

  /** The sample saves in the repository root. Tests run from the app folder. */
  private static final Path SAMPLES = Path.of("../samples");

  private final SaveLoader loader = new SaveLoader();

  @TempDir
  private Path tempFolder;

  @Test
  @DisplayName("should load a valid save with its file name and trainer")
  void loadsValidSave() throws SaveLoadException {
    GameSave save = loader.load(SAMPLES.resolve("red.sav"));

    assertEquals("red.sav", save.getFileName());
    assertEquals("A", save.getTrainerName());
  }

  @Test
  @DisplayName("should tell which file is missing")
  void throwsForMissingFile() {
    Path missing = tempFolder.resolve("missing.sav");

    SaveLoadException exception =
        assertThrows(SaveLoadException.class, () -> loader.load(missing));

    assertEquals("File not found: " + missing, exception.getMessage());
    assertInstanceOf(NoSuchFileException.class, exception.getCause());
  }

  @Test
  @DisplayName("should tell which file is not a valid save")
  void throwsForInvalidSave() throws IOException {
    Path notASave = tempFolder.resolve("notes.txt");
    Files.writeString(notASave, "This is not a save file");

    SaveLoadException exception =
        assertThrows(SaveLoadException.class, () -> loader.load(notASave));

    // Only the start is ours; the rest comes from savechecker, whose wording may change.
    assertTrue(exception.getMessage().startsWith("Not a valid save file: notes.txt: "));
    assertInstanceOf(InvalidSaveFileException.class, exception.getCause());
  }

  @Test
  @DisplayName("should tell which path could not be read")
  void throwsForUnreadablePath() {
    SaveLoadException exception =
        assertThrows(SaveLoadException.class, () -> loader.load(tempFolder));

    assertEquals("Could not read " + tempFolder, exception.getMessage());
    assertInstanceOf(IOException.class, exception.getCause());
  }
}
