package uk.iatom.iAtomSys.common.api;

public record MemoryRequestPacket(Short pcrOffset, Short sliceWidth) {

  public static final int MAX_SLICE_WIDTH = 512;

  public static String ERR_WIDTH(Short sliceWidth) {
    return "Slice width %d exceeds interval [0,%d]".formatted(sliceWidth, MAX_SLICE_WIDTH);
  }

  public MemoryRequestPacket {

    if (sliceWidth < 0 || sliceWidth > MAX_SLICE_WIDTH) {
      throw new IllegalArgumentException(ERR_WIDTH(sliceWidth));
    }
  }
}
