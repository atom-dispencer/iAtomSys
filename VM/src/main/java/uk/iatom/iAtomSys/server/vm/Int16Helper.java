package uk.iatom.iAtomSys.server.vm;

public class Int16Helper {

  public static final int INT_16_MAX = (int) Math.pow(2, 16) - 1;

  public static int bytesToInt16(final byte[] bytes) {
    assert bytes.length == 2;
    return bytesToInt16(bytes[0], bytes[1]);
  }

  public static int bytesToInt16(final byte msByte, final byte lsByte) {
    return (msByte << 8) | lsByte;
  }

  public static byte[] int16ToBytes(final int int16) {
    assert int16 < Math.pow(2, 16);
    return new byte[]{(byte) (int16 >> 8), (byte) (int16 | 0x000000ff)};
  }
}
