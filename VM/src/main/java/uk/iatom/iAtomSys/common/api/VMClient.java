package uk.iatom.iAtomSys.common.api;

public interface VMClient {

  VMStateResponsePacket getState(VMStateRequestPacket packet);

  String step(StepRequestPacket packet);

  String loadmem(LoadRequestPacket packet);

  String set(SetRequestPacket packet);
}
