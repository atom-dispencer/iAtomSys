package uk.iatom.iAtomSys.common.api;

import uk.iatom.iAtomSys.common.instruction.FlagHelper;

// TODO PortPacket needs to be implemented on BOTH client and server.
public record PortPacket(int id, char address, char value, FlagHelper.Flag flag) {

}
