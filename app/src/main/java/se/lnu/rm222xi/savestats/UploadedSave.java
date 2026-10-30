package se.lnu.rm222xi.savestats;

/**
 * A save file sent from the browser, not yet checked to be a valid save.
 *
 * @param fileName the name of the file on the user's computer
 * @param data the contents of the file
 */
record UploadedSave(String fileName, byte[] data) {
}
