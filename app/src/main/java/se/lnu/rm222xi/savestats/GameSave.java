package se.lnu.rm222xi.savestats;

import se.lnu.savechecker.SaveFile;

/**
 * One loaded save file, seen through the questions this application asks. Wraps the savechecker
 * module so that the rest of the application never depends on its classes.
 */
class GameSave {

  private final String fileName;
  private final SaveFile saveFile;

  /**
   * Creates a save from an already validated save file.
   *
   * @param fileName the name of the file, shown to the user
   * @param saveFile the validated save
   */
  GameSave(String fileName, SaveFile saveFile) {
    this.fileName = fileName;
    this.saveFile = saveFile;
  }

  String getFileName() {
    return fileName;
  }

  String getTrainerName() {
    return saveFile.getTrainer().getName();
  }
}
