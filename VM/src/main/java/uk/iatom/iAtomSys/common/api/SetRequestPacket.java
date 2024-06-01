package uk.iatom.iAtomSys.common.api;

/**
 * @param address The address which you wish to set the value at, in the same format as
 *                assembly/disassembled code. This may include references to registers.
 * @param value   The value to put into the given memorySlice address.
 */
public record SetRequestPacket(String address, String value) {

}
