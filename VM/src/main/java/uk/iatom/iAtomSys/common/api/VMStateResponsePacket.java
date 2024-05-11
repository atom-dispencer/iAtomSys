package uk.iatom.iAtomSys.common.api;

import java.util.List;
import uk.iatom.iAtomSys.client.disassembly.RegisterPacket;

public record VMStateResponsePacket(
    short memoryStartAddress,
    short[] memory,
    List<RegisterPacket> registers
) {

}