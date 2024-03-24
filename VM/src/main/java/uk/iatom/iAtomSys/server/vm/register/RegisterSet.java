package uk.iatom.iAtomSys.server.vm.register;

public record RegisterSet(
    Register ProgramCounter,
    Register Accumulator,
    Register Return,
    Register IOStack,
    Register IOMemory,
    Register Pointer,
    Register Numeric,
    Register Idk,
    Register Flags
) {

  public static final int INDEX_PROGRAM_COUNTER = 0;
  public static final int INDEX_ACCUMULATOR = 1;
  public static final int INDEX_RETURN = 2;

  public static final int INDEX_IO_STACK = 3;
  public static final int INDEX_IO_MEMORY = 4;
  public static final int INDEX_POINTER = 5;

  public static final int INDEX_NUMERIC = 6;
  public static final int INDEX_IDK = 7;
  public static final int INDEX_FLAGS = 8;

  /**
   * Convenience method as it may not be apparent that one can simply cast the byte to an int to use
   * as an index.
   *
   * @param b The index of the register to fetch, as a byte.
   * @return The {@link Register} in this {@link RegisterSet} corresponding to the given index.
   */
  public Register fromByte(byte b) {
    return fromIndex(b);
  }

  /**
   * @return The {@link Register} in this {@link RegisterSet} corresponding to the given integer
   * index.
   */
  public Register fromIndex(int index) {
    return switch (index) {
      case INDEX_PROGRAM_COUNTER -> ProgramCounter;
      case INDEX_ACCUMULATOR -> Accumulator;
      case INDEX_RETURN -> Return;
      case INDEX_IO_STACK -> IOStack;
      case INDEX_IO_MEMORY -> IOMemory;
      case INDEX_POINTER -> Pointer;
      case INDEX_NUMERIC -> Numeric;
      case INDEX_IDK -> Idk;
      case INDEX_FLAGS -> Flags;
      default ->
          throw new IndexOutOfBoundsException("%d is not a valid register index".formatted(index));
    };
  }
}
