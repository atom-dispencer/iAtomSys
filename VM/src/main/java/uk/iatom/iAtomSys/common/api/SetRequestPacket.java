package uk.iatom.iAtomSys.common.api;

/**
 * @param address The address which you wish to set the value at, in the same format as
 *                assembly/disassembled code. This may include references to registers.
 * @param value   The value to put into the given memorySlice address.
 */
public record SetRequestPacket(String address, String value) {

  public SetRequestPacket {
    if (address == null) {
      throw new IllegalStateException("Set command address may not be null");
    }
    if (value == null) {
      throw new IllegalStateException("Set command value may not be null");
    }

    if (address.isBlank()) {
      throw new IllegalStateException("Set command address may not be blank");
    }
    if (value.isBlank()) {
      throw new IllegalStateException("Set command value may not be blank");
    }
  }
}
