package uk.iatom.iAtomSys.common.register;


import lombok.Getter;
import uk.iatom.iAtomSys.common.api.RegisterPacket;
import uk.iatom.iAtomSys.server.memory.Memory;

public class Register {

  @Getter
  private final String name;
  @Getter
  private final int id;
  @Getter
  private final char address;
  private final Memory memory;

  public Register(String name, int id, char address, Memory memory) {
    this.name = name;
    this.id = id;
    this.address = address;
    this.memory = memory;
  }

  public static Register ACC(RegisterSet registerSet) {
    return registerSet.getRegister("ACC");
  }

  public static Register IDK(RegisterSet registerSet) {
    return registerSet.getRegister("IDK");
  }

  public static Register TBH(RegisterSet registerSet) {
    return registerSet.getRegister("TBH");
  }

  public static Register LOL(RegisterSet registerSet) {
    return registerSet.getRegister("LOL");
  }

  public static Register PCR(RegisterSet registerSet) {
    return registerSet.getRegister("PCR");
  }

  public static Register FLG(RegisterSet registerSet) {
    return registerSet.getRegister("FLG");
  }

  public char get() {
    return memory.read(this.address);
  }

  public void set(char value) {
    memory.write(this.address, value);
  }

  public RegisterPacket toPacket() {
    return new RegisterPacket(name, id, address, get());
  }
}
