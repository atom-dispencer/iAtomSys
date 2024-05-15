package uk.iatom.iAtomSys.server.device;

import java.util.ArrayList;
import java.util.List;

public class BufferedIODevice implements Device {

  private List<Short> buffer = new ArrayList<>();
  private int readPointer = 0;

  @Override
  public void deviceWrite(short s) {
    buffer.add(s);
  }

  @Override
  public short deviceRead() {
    if (readPointer < buffer.size()) {
      return buffer.get(readPointer++);
    }
    return 0;
  }
}
