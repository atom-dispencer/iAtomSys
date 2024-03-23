package uk.iatom.iAtomSys.server.vm.register;


import uk.iatom.iAtomSys.server.vm.memory.DataSizes;
import uk.iatom.iAtomSys.server.vm.memory.Memory;

public class InMemoryRegister implements Register {

  private Memory memory;
  private DataSizes dataSizes;
  private int address;

  public InMemoryRegister(int address, DataSizes dataSizes, Memory memory) {
    this.address = address;
    this.dataSizes = dataSizes;
    this.memory = memory;
  }

  @Override
  public int get() {
    byte[] bytes = memory.read(this.address, dataSizes.integerBytes());
    int result = 0;
    for (byte b : bytes) {
      result = (result << 8) + (result | b);
    }
    return result;
  }

  @Override
  public void set(int value) {
    byte[] bytes = new byte[Integer.BYTES];
    int length = bytes.length;
    for (int i = 0; i < length; i++) {
      bytes[length - i - 1] = (byte) (value & 0xFF);
      value >>= 8;
    }
    memory.write(this.address, bytes);
  }
}
