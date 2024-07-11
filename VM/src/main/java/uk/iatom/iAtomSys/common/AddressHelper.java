package uk.iatom.iAtomSys.common;

public class AddressHelper {

  public static int MEMORY_SPACE = Character.MAX_VALUE + 1;

  public static char hexToInt16(String str) throws AddressFormatException {
    return parseInt16(str, 16);
  }

  public static char parseInt16(String str, int radix) throws AddressFormatException {
    try {
      int i = Integer.parseInt(str, radix);

      if (i > Character.MAX_VALUE) {
        throw new AddressFormatException(
            "%s exceeds the maximum value for an int-16.".formatted(str));
      }

      return (char) i;
    } catch (NumberFormatException nfx) {
      throw new AddressFormatException(nfx);
    }
  }

  public static char fromBytes(byte left, byte right) {
    return (char) ((Byte.toUnsignedInt(left) << 8) | Byte.toUnsignedInt(right));
  }
}
