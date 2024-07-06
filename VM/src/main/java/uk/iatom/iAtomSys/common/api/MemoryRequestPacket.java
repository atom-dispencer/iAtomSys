package uk.iatom.iAtomSys.common.api;

public record MemoryRequestPacket(int pcrOffset, char sliceWidth) {

  public static final int MAX_SLICE_WIDTH = 512;

  public static String ERR_WIDTH(char sliceWidth) {
    return "Slice width %d exceeds interval [0,%d]".formatted((int) sliceWidth, MAX_SLICE_WIDTH);
  }

  public MemoryRequestPacket {

    if (sliceWidth > MAX_SLICE_WIDTH) {
      throw new IllegalArgumentException(ERR_WIDTH(sliceWidth));
    }
  }
}
