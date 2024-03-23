package uk.iatom.iAtomSys.server.vm.memory;

public interface Memory {

  byte[] read(int address, int width);

  void write(int address, byte[] bytes);

  int getSize();
}
