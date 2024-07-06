package uk.iatom.iAtomSys.server.memory;

public class Memory {

  private final char[] buffer;

  public Memory() {
    this.buffer = new char[0xffff];
  }

  public char read(char address) {

    // Can't return anything if addressBytes out of bounds
    if (address >= getSize()) {
      throw new IndexOutOfBoundsException(
          "The given address %d is outside the memorySlice space %d".formatted((int) address,
              Short.toUnsignedInt(Short.MAX_VALUE)));
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
              (int) startAddress, endAddress, Short.toUnsignedInt(Short.MAX_VALUE)));
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
              (int) startAddress, endAddress, Short.toUnsignedInt(Short.MAX_VALUE)));
    }

    System.arraycopy(shorts, 0, buffer, startAddress, width);
  }

  public int getSize() {
    return buffer.length;
  }
}
