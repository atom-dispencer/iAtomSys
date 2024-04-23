package uk.iatom.iAtomSys.common.configuration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import uk.iatom.iAtomSys.client.disassembly.InstructionDisassembler;
import uk.iatom.iAtomSys.common.instruction.DuplicateInstructionException;
import uk.iatom.iAtomSys.common.instruction.InstructionSet;
import uk.iatom.iAtomSys.common.register.DuplicateRegisterException;
import uk.iatom.iAtomSys.common.register.RegisterSet;
import uk.iatom.iAtomSys.server.instruction.InstructionExecutor;
import uk.iatom.iAtomSys.server.memory.Memory;

@Configuration
public class CommonBeans {

  //TODO Implement correct disassemblers for known flag layouts
  @Bean
  @Scope(BeanDefinition.SCOPE_SINGLETON)
  public InstructionSet instructionSet() throws DuplicateInstructionException {
    InstructionSet set = new InstructionSet();

    set.createInstruction("NOP", (byte) 0x00, InstructionDisassembler::noFlags, InstructionExecutor::xNOP);
    set.createInstruction("MOV", (byte) 0x01, InstructionDisassembler::twoRegisters_02_35, InstructionExecutor::xMOV);
    set.createInstruction("FLG", (byte) 0x02, InstructionDisassembler::dFLG, InstructionExecutor::xFLG);
    set.createInstruction("PSH", (byte) 0x04, InstructionDisassembler::oneRegister_02, InstructionExecutor::xPSH);
    set.createInstruction("POP", (byte) 0x05, InstructionDisassembler::oneRegister_02, InstructionExecutor::xPOP);
    set.createInstruction("INC", (byte) 0x06, InstructionDisassembler::oneRegister_02, InstructionExecutor::xINC);
    set.createInstruction("DEC", (byte) 0x07, InstructionDisassembler::oneRegister_02, InstructionExecutor::xDEC);
    set.createInstruction("ADD", (byte) 0x08, InstructionDisassembler::oneRegister_02, InstructionExecutor::xADD);
    set.createInstruction("SUB", (byte) 0x09, InstructionDisassembler::oneRegister_02, InstructionExecutor::xSUB);
    set.createInstruction("ZRO", (byte) 0x0a, InstructionDisassembler::oneRegister_02, InstructionExecutor::xZRO);

    return set;
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_SINGLETON)
  public RegisterSet registerSet(Memory memory) throws DuplicateRegisterException {
    RegisterSet set = new RegisterSet(memory, (short) 0x0010);

    // Standard registers
    set.createRegister("ACC", 0);
    set.createRegister("IDK", 1);
    set.createRegister("TBH", 2);
    set.createRegister("LOL", 3);

    // Hidden registers
    set.createRegister("PCR", 4);
    set.createRegister("FLG", 5);

    return set;
  }
}
