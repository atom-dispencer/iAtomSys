package uk.iatom.iAtomSys.common.api;

public record StepRequestPacket(int count) {

  public static final int MIN_COUNT = 1;
  public static final int MAX_COUNT = 256;

  public StepRequestPacket {
    if (count < MIN_COUNT || count > MAX_COUNT) {
      throw new IllegalArgumentException(BAD_INTERVAL(count));
    }
  }

  public static String BAD_INTERVAL(int count) {
    return "Count must be in interval [%d,%d], got %d".formatted(MIN_COUNT, MAX_COUNT, count);
  }
}
