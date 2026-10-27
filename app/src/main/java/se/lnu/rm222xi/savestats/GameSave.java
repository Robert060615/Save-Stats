package se.lnu.rm222xi.savestats;

import java.util.List;
import se.lnu.savechecker.PartyPokemon;
import se.lnu.savechecker.SaveFile;

/**
 * One loaded save file, seen through the questions this application asks. Wraps the savechecker
 * module so that the rest of the application never depends on its classes.
 */
class GameSave {

  private static final int POKEDEX_SIZE = 151;

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

  int getSeenCount() {
    return saveFile.getPokedexStatus().getSeenCount();
  }

  int getCaughtCount() {
    return saveFile.getPokedexStatus().getCaughtCount();
  }

  int getPokedexSize() {
    return POKEDEX_SIZE;
  }

  boolean hasParty() {
    return !partyMembers().isEmpty();
  }

  List<String> getPartyNicknames() {
    return partyMembers().stream().map(PartyPokemon::getNickname).toList();
  }

  List<Double> getPartyLevels() {
    return partyMembers().stream().map(pokemon -> (double) pokemon.getLevel()).toList();
  }

  private List<PartyPokemon> partyMembers() {
    return saveFile.getParty().getPokemon();
  }
}
