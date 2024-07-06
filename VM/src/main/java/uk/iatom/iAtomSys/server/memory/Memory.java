package uk.iatom.iAtomSys.server.memory;

import uk.iatom.iAtomSys.common.Int16Helper;

public class Memory {

  private final char[] buffer;

  public Memory() {
    this.buffer = new char[Int16Helper.MEMORY_SPACE];
  }

  public char read(char address) {

    // Can't return anything if addressBytes out of bounds
    if (address > getSize()) {
      throw new IndexOutOfBoundsException(
          "The given address %d is outside the memorySlice space %d".formatted((int) address, buffer.length));
    }

    return buffer[address];
  }

  public void readRange(char startAddress, char[] destination) {

    // Java shorts are signed, but we need it to be unsigned.
    int width = destination.length;
    int endAddress = startAddress + width;

    // Can't return anything if the region is out of bounds
    // endAddress >= startAddress, so only need to check end
    if (endAddress >= getSize()) {
      throw new IndexOutOfBoundsException(
          "Some or all of the write region [%d,%d] lies outside the memorySlice space %d".formatted(
              (int) startAddress, endAddress, buffer.length));
    }

    System.arraycopy(buffer, startAddress, destination, 0, width);
  }

  public void write(char startAddress, char... shorts) {

    // Java shorts are signed, but we need it to be unsigned.
    int width = shorts.length;
    int endAddress = startAddress + width - 1;

    // Can't return anything if the region is out of bounds
    // endAddress >= startAddress, so only need to check end
    if (endAddress >= getSize()) {
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
