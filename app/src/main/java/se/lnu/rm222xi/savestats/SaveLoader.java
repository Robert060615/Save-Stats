package se.lnu.rm222xi.savestats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import se.lnu.savechecker.InvalidSaveFileException;
import se.lnu.savechecker.SaveFile;

/**
 * Reads save files from disk or from bytes already in memory. This is the only class that knows
 * how the savechecker module is created and which exceptions it throws.
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
    try {
      return load(path.getFileName().toString(), Files.readAllBytes(path));
    } catch (NoSuchFileException e) {
      throw new SaveLoadException("File not found: " + path, e);
    } catch (IOException e) {
      throw new SaveLoadException("Could not read " + path, e);
    }
  }

  /**
   * Validates a save whose contents are already in memory, for example a file sent from a browser.
   *
   * @param fileName the name to show for the save in reports and error messages
   * @param data the raw contents of the save file
   * @return the loaded save
   * @throws SaveLoadException if the data is not a valid save
   */
  GameSave load(String fileName, byte[] data) throws SaveLoadException {
    try {
      return new GameSave(fileName, new SaveFile(data));
    } catch (InvalidSaveFileException e) {
      throw new SaveLoadException("Not a valid save file: " + fileName + ": " + e.getMessage(), e);
    }
  }
}
