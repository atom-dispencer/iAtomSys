package uk.iatom.iAtomSys.common.api;

public record VMStateRequestPacket(Short pcrOffset, Short sliceWidth) {

  public static final int MAX_SLICE_WIDTH = 512;

  public VMStateRequestPacket {

//    if (sliceWidth < 0 || sliceWidth > MAX_SLICE_WIDTH) {
//      throw new IllegalArgumentException("Slice width %d exceeds interval [0,%d]".formatted(
//          sliceWidth, MAX_SLICE_WIDTH));
//    }
  }
}
