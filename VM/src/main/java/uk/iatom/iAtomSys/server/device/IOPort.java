package uk.iatom.iAtomSys.server.device;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import uk.iatom.iAtomSys.common.instruction.FlagHelper;
import uk.iatom.iAtomSys.common.register.RegisterSet;
import uk.iatom.iAtomSys.server.memory.Memory;

public class IOPort {

  @Getter
  private final short address;
  @Getter
  private final FlagHelper.Flag flag;
  private final RegisterSet registerSet;
  private final Memory memory;

  private final ArrayList<Short> outputBuffer = new ArrayList<>();
  private int outputPointer = 0;
  private final ArrayList<Short> inputBuffer = new ArrayList<>();
  private int inputPointer = 0;


  public IOPort(short address, FlagHelper.Flag flag, RegisterSet registerSet, Memory memory) {
    this.address = address;
    this.flag = flag;
    this.registerSet = registerSet;
    this.memory = memory;
  }

  /**
   * Update the input and output values of the {@link IOPort}, including its bound
   * {@link IOPort#address} and {@link IOPort#flag}.
   * <ol>
   *   <li>If the {@link IOPort#flag} is HIGH, read the value at the bound {@link IOPort#address}
   *   into the {@link IOPort#outputBuffer}.</li>
   *   <li>If the {@link IOPort#inputPointer} has not caught up with the {@link IOPort#inputBuffer},
   *   write the next value in the {@link IOPort#inputBuffer} to the bound {@link IOPort#address}
   *   and set the {@link IOPort#flag} HIGH, otherwise set it LOW.</li>
   * </ol>
   * <br>
   */
  public void updateFlag() {
    if (hasUnreadInput()) {
      FlagHelper.setFlag(registerSet, flag.bitIndex, true);
    } else {
      FlagHelper.setFlag(registerSet, flag.bitIndex, false);
    }
  }


  /**
   * Shuffle the next value from the {@link IOPort#inputBuffer} into the bound
   * {@link IOPort#address}.
   */
  public void shuffleInput() {
    short readValue = hasUnreadInput() ? inputBuffer.get(inputPointer++) : 0;
    memory.write(address, readValue);
  }

  /**
   * Shuffle the value at the bound {@link IOPort#address} into the {@link IOPort#outputBuffer}.
   */
  public void shuffleOutput() {
    short writeValue = memory.read(address);
    outputBuffer.add(writeValue);
  }

  public boolean hasUnreadInput() {
    return inputPointer < inputBuffer.size() - 1;
  }

  public boolean hasUnreadOutput() {
    return outputPointer < outputBuffer.size() - 1;
  }

  public void writeInput(short s) {
    inputBuffer.add(s);
  }

  public void writeInput(List<Short> data) {
    inputBuffer.addAll(data);
  }

  public List<Short> readUnreadOutput() {
    inputPointer = inputBuffer.size() - 1;
    return inputBuffer.subList(inputPointer, inputBuffer.size() - 1);
  }

  public List<Short> readAllOutput() {
    inputPointer = inputBuffer.size() - 1;
    return outputBuffer.subList(0, outputBuffer.size() - 1) ;
  }
}