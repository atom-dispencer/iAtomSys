package uk.iatom.iAtomSys.common.api;

public record MemoryResponsePacket(
    // A contiguous slice (region) of memory, starting at the given address
    char sliceStartAddress,
    char[] memorySlice
) {

}