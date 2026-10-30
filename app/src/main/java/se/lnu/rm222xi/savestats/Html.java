package se.lnu.rm222xi.savestats;

/**
 * Helps put plain text into HTML pages.
 */
final class Html {

  private Html() {
  }

  /**
   * Escapes the characters that have a meaning in HTML, so text from a save file or from the user
   * is always shown as text and never read as markup.
   *
   * @param text plain text
   * @return the same text, safe to put between HTML tags
   */
  static String escape(String text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
