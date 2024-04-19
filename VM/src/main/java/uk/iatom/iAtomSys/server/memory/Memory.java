package uk.iatom.iAtomSys.server.memory;

public class Memory {

  private final short[] buffer;

  public Memory() {
    this.buffer = new short[Short.toUnsignedInt(Short.MAX_VALUE)];
  }

  public short read(short address) {

    // Java shorts are signed, but we need it to be unsigned.
    int unsignedAddress = Short.toUnsignedInt(address);

    // Can't return anything if addressBytes out of bounds
    if (unsignedAddress >= getSize()) {
      throw new IndexOutOfBoundsException("The given address %d is outside the memory space %d".formatted(unsignedAddress, Short.toUnsignedInt(Short.MAX_VALUE)));
    }

    return buffer[unsignedAddress];
  }

  public void readRange(short s16_address, short[] destination) {

    // Java shorts are signed, but we need it to be unsigned.
    int u32_startAddress = Short.toUnsignedInt(s16_address);
    int width = destination.length;
    int u32_endAddress = u32_startAddress + width;

    // Can't return anything if the region is out of bounds
    // endAddress >= startAddress, so only need to check end
    if (u32_endAddress >= getSize()) {
      throw new IndexOutOfBoundsException("Some or all of the write region [%d,%d] lies outside the memory space %d".formatted(u32_startAddress, u32_endAddress, Short.toUnsignedInt(Short.MAX_VALUE)));
    }

    System.arraycopy(buffer, s16_address, destination, 0, width);
  }

  public void write(short s16_address, short... shorts) {

    // Java shorts are signed, but we need it to be unsigned.
    int u32_startAddress = Short.toUnsignedInt(s16_address);
    int width = shorts.length;
    int u32_endAddress = u32_startAddress + width;

    // Can't return anything if the region is out of bounds
    // endAddress >= startAddress, so only need to check end
    if (u32_endAddress >= getSize()) {
      throw new IndexOutOfBoundsException("Some or all of the write region [%d,%d] lies outside the memory space %d".formatted(u32_startAddress, u32_endAddress, Short.toUnsignedInt(Short.MAX_VALUE)));
    }

    System.arraycopy(shorts, 0, buffer, s16_address, width);
  }

  public int getSize() {
    return buffer.length;
  }
}
