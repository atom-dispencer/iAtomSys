package uk.iatom.iAtomSys.common;

public class Int16Helper {

  public static char hexToInt16(String str) {
    return parseInt16(str, 16);
  }

  public static char parseInt16(String str, int radix) {
    int i = Integer.parseInt(str, radix);

    if (i > Character.MAX_VALUE) {
      throw new NumberFormatException("%s exceeds the maximum value for an int-16.".formatted(str));
    }

    return (char) i;
  }
}
