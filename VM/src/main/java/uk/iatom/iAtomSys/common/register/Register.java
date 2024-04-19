package uk.iatom.iAtomSys.common.register;


import lombok.Getter;
import uk.iatom.iAtomSys.server.memory.Memory;

public class Register {

  public static Register ACC(RegisterSet registerSet){
    return registerSet.getRegister("ACC");
  }
  public static Register IDK(RegisterSet registerSet) {
    return registerSet.getRegister("IDK");
  }
  public static Register TBH (RegisterSet registerSet) {
    return registerSet.getRegister("TBH");
  }
  public static Register LOL (RegisterSet registerSet){
    return registerSet.getRegister("LOL");
  }
  public static Register PCR (RegisterSet registerSet){
    return registerSet.getRegister("PCR");
  }
  public static Register FLG (RegisterSet registerSet){
    return registerSet.getRegister("FLG");
  }

  @Getter private final String name;
  @Getter private final int id;
  @Getter private final short address;
  private final Memory memory;

  public Register(String name, int id, short address, Memory memory) {
    this.name = name;
    this.id = id;
    this.address = address;
    this.memory = memory;
  }

  public short get() {
    return memory.read(this.address);
  }

  public void set(short value) {
    memory.write(this.address, value);
  }
}
