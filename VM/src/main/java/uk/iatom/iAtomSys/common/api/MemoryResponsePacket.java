package uk.iatom.iAtomSys.common.api;

public record MemoryResponsePacket(
    // A contiguous slice (region) of memory, starting at the given address
    char sliceStartAddress,
    char[] memorySlice

    // Any time the general memory state is read, the registers will need to be updated anyway
    // But they should be logically separated for API consistency.
//    List<RegisterPacket> registers,

    // Misc state
//    List<String> availableImages,

    // Static state
//    List<Short> orderedPortAddresses
    // Debug symbols
) {

}