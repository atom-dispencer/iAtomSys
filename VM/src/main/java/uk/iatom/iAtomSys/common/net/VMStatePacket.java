package uk.iatom.iAtomSys.common.net;

import java.util.List;
import uk.iatom.iAtomSys.client.disassembly.RegisterPacket;

public record VMStatePacket(
    short[] memory,
    List<RegisterPacket> registers
) {

}