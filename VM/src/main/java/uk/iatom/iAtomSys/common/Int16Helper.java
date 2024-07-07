package uk.iatom.iAtomSys.common;

public class Int16Helper {

  public static int MEMORY_SPACE = Character.MAX_VALUE + 1;

  public static char hexToInt16(String str) throws NumberFormatException {
    return parseInt16(str, 16);
  }

  public static char parseInt16(String str, int radix) throws NumberFormatException {
    int i = Integer.parseInt(str, radix);

    if (i > Character.MAX_VALUE) {
      throw new NumberFormatException("%s exceeds the maximum value for an int-16.".formatted(str));
    }

    return (char) i;
  }

  public static char fromBytes(byte left, byte right) {
    return (char) ((Byte.toUnsignedInt(left) << 8) | Byte.toUnsignedInt(right));
  }
}
