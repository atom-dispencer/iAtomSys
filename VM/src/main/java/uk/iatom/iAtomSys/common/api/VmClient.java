package uk.iatom.iAtomSys.common.api;

import java.util.List;

public interface VmClient {

  // Queries
  VmStatus getStatus();

  MemoryResponsePacket getMemory(MemoryRequestPacket packet);

  List<RegisterPacket> getRegisters();

  List<PortPacket> getPorts();

  // Commands
  String step(StepRequestPacket packet);

  String loadmem(LoadRequestPacket packet);

  String set(SetRequestPacket packet);
}
