package uk.iatom.iAtomSys.common.api;

public interface VMClient {

  VMStateResponsePacket getState(VMStateRequestPacket packet);

  String step(StepRequestPacket packet);

  String loadmem(LoadImageRequestPacket packet);
}
