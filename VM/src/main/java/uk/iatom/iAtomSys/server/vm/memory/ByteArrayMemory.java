package uk.iatom.iAtomSys.server.vm.memory;

public class ByteArrayMemory implements Memory {

  private final byte[] buffer;

  public ByteArrayMemory(byte[] buffer) {
    this.buffer = buffer;
  }

  @Override
  public byte[] read(int address, int width) {

    // Can't return anything if addressBytes out of bounds
    if (address < 0 || address >= getSize()) {
      return new byte[0];
    }

    // Return as much as possible if width out of bounds
    if (address + width >= getSize()) {
      width = (int) getSize() - address;
    }

    byte[] result = new byte[width];
    System.arraycopy(this.buffer, address, result, 0, width);
    return result;
  }

  @Override
  public void write(int address, byte[] bytes) {

    // Can't return anything if addressBytes out of bounds
    if (address < 0 || address >= getSize()) {
      return;
    }

    int width = bytes.length;
    // Return as much as possible if width out of bounds
    if (address + width >= getSize()) {
      width = getSize() - address;
    }

    System.arraycopy(bytes, 0, buffer, address, width);
  }

  @Override
  public int getSize() {
    return buffer.length;
  }
}
