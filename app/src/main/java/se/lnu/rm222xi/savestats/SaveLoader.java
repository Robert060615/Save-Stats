package se.lnu.rm222xi.savestats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import se.lnu.savechecker.InvalidSaveFileException;
import se.lnu.savechecker.SaveFile;

/**
 * Reads save files from disk. This is the only class that knows how the savechecker module is
 * created and which exceptions it throws.
 */
class SaveLoader {

  /**
   * Reads and validates the save file at the given path.
   *
   * @param path where the save file is
   * @return the loaded save
   * @throws SaveLoadException if the file is missing, cannot be read or is not a valid save
   */
  GameSave load(Path path) throws SaveLoadException {
    String fileName = path.getFileName().toString();
    try {
      return new GameSave(fileName, new SaveFile(Files.readAllBytes(path)));
    } catch (NoSuchFileException e) {
      throw new SaveLoadException("File not found: " + path, e);
    } catch (IOException e) {
      throw new SaveLoadException("Could not read " + path, e);
    } catch (InvalidSaveFileException e) {
      throw new SaveLoadException("Not a valid save file: " + fileName + ": " + e.getMessage(), e);
    }
  }
}
