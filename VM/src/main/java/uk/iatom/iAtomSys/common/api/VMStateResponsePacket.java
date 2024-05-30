package uk.iatom.iAtomSys.common.api;

import java.util.List;
import uk.iatom.iAtomSys.client.disassembly.RegisterPacket;

public record VMStateResponsePacket(
    List<String> availableImages,
    short memoryStartAddress,
    short[] memory,
    List<RegisterPacket> registers,
    List<Short> orderedPortAddresses
) {

}