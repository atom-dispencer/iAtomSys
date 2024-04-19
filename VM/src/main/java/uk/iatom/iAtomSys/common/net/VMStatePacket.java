package uk.iatom.iAtomSys.common.net;

import java.util.Map;

public record VMStatePacket(
    short[] memory,
    Map<String, Short> registers
) {

}