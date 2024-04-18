package uk.iatom.iAtomSys.api;

import java.util.Map;

public record VMStatePacket(
    byte[] memory,
    Map<String, Integer> registers
) {

}