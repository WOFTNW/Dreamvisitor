package io.github.stonley890.dreamvisitor.util;
public class UUIDFromater {
  /**
   * Adds the hyphens back into a String UUID.
   *
   * @param uuid the UUID as a {@link String} without hyphens.
   * @return a UUID as a string with hyphens.
   */
  public static  String formatUuid(String uuid) {

    return uuid.replaceFirst(
        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
        "$1-$2-$3-$4-$5");
  }
}
