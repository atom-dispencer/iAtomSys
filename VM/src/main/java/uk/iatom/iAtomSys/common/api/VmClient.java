package uk.iatom.iAtomSys.common.api;

public interface VmClient {

  VmStatus getStatus();

  MemoryResponsePacket getMemory(MemoryRequestPacket packet);

  String step(StepRequestPacket packet);

  String loadmem(LoadRequestPacket packet);

  String set(SetRequestPacket packet);
}
