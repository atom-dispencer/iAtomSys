package uk.iatom.iAtomSys.common.api;

public interface VmClient {

  // Queries
  VmStatus getStatus();

  String[] getAvailableImages();

  MemoryResponsePacket getMemory(MemoryRequestPacket packet);

  RegisterPacket[] getRegisters();

  PortPacket[] getPorts();

  RunningDataPacket getRunningData();

  // Commands
  String step(StepRequestPacket packet);

  String load(LoadRequestPacket packet);

  String set(SetRequestPacket packet);

  String drop_debug();

  String run(RunRequestPacket runRequestPacket);

  String pause();

  String tbreak(ToggleBreakpointRequestPacket addressStr);
}
