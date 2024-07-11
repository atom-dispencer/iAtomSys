package uk.iatom.iAtomSys.server.memory;

import uk.iatom.iAtomSys.common.AddressHelper;

public class Memory {

  private final char[] buffer;

  public Memory() {
    this.buffer = new char[AddressHelper.MEMORY_SPACE];
  }

  public char read(char address) {

    // Can't return anything if addressBytes out of bounds
    if (address > getSize()) {
      throw new IndexOutOfBoundsException(
          "The given address %d is outside the memorySlice space %d".formatted((int) address,
              buffer.length));
    }

    return buffer[address];
  }

  public void readRange(char startAddress, char[] destination) {
    int width = destination.length;
    int endAddress = startAddress + width;

    // endAddress always >= startAddress, so only need to check end
    // Max end address is 0xFFFF, and memory space is 0xFFFF, so must be strictly greater
    if (endAddress > getSize()) {
      throw new IndexOutOfBoundsException(
          "Some or all of the read region [%d,%d] lies outside the memorySlice space %d".formatted(
              (int) startAddress, endAddress, buffer.length));
    }

    System.arraycopy(buffer, startAddress, destination, 0, width);
  }

  public void write(char startAddress, char... shorts) {
    int width = shorts.length;
    int endAddress = startAddress + width - 1;

    // endAddress always >= startAddress, so only need to check end point
    // Max end address is 0xFFFF, and memory space is 0xFFFF, so must be strictly greater
    if (endAddress > getSize()) {
      throw new IndexOutOfBoundsException(
          "Some or all of the write region [%d,%d] lies outside the memorySlice space %d".formatted(
              (int) startAddress, endAddress, buffer.length));
    }

    System.arraycopy(shorts, 0, buffer, startAddress, width);
  }

  public int getSize() {
    return buffer.length;
  }
}
