package uk.iatom.iAtomSys.client.disassembly;

import lombok.ToString;

public record RegisterPacket(String name, int id, short address, short value) {

}
