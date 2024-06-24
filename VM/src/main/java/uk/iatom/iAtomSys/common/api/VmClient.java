package uk.iatom.iAtomSys.common.api;

public interface VmClient {

  // Queries
  VmStatus getStatus();

  MemoryResponsePacket getMemory(MemoryRequestPacket packet);

  RegisterPacket[] getRegisters();

  PortPacket[] getPorts();

  // Commands
  String step(StepRequestPacket packet);

  String loadmem(LoadRequestPacket packet);

  String set(SetRequestPacket packet);
}
