package uk.iatom.iAtomSys.server.vm.register;

public record RegisterSet(
    Register ProgramCounter,
    Register Accumulator,
    Register Return,
    Register Pop,
    Register Numeric,
    Register A
) {

  public static final int PROGRAM_COUNTER_INDEX = 0;
  public static final int ACCUMULATOR_INDEX = 1;
  public static final int RETURN_INDEX = 2;

  public static final int POP_INDEX = 3;

  public static final int NUMERIC_INDEX = 4;
  public static final int A_INDEX = 5;

  public Register fromIndex(int index) {
    return switch (index) {
      case PROGRAM_COUNTER_INDEX -> ProgramCounter;
      case ACCUMULATOR_INDEX -> Accumulator;
      case RETURN_INDEX -> Return;
      case POP_INDEX -> Pop;
      case NUMERIC_INDEX -> Numeric;
      case A_INDEX -> A;
      default -> null;
    };
  }
}
