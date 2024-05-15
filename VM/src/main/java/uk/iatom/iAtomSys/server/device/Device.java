package uk.iatom.iAtomSys.server.device;

public interface Device {
  void deviceWrite(short s);

  short deviceRead();
}
