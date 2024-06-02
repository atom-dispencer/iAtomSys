package uk.iatom.iAtomSys.common.api;

public record DebugSymbolsRequestPacket(short startAddress, short endAddress) {

  public DebugSymbolsRequestPacket {
    if (endAddress < startAddress) {
      throw new IllegalArgumentException("The end address %d cannot be < the start address %d.".formatted(startAddress, endAddress));
    }
  }

}
