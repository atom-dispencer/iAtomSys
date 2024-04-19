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
    set.createInstruction("MOV", (byte) 0x01, InstructionDisassembler::twoRegisters, InstructionExecutor::xMOV);
    set.createInstruction("PSH", (byte) 0x04, InstructionDisassembler::noFlags, InstructionExecutor::xPSH);
    set.createInstruction("POP", (byte) 0x05, InstructionDisassembler::noFlags, InstructionExecutor::xPOP);
    set.createInstruction("INC", (byte) 0x06, InstructionDisassembler::noFlags, InstructionExecutor::xINC);
    set.createInstruction("DEC", (byte) 0x07, InstructionDisassembler::noFlags, InstructionExecutor::xDEC);
    set.createInstruction("ADD", (byte) 0x08, InstructionDisassembler::noFlags, InstructionExecutor::xADD);
    set.createInstruction("SUB", (byte) 0x09, InstructionDisassembler::noFlags, InstructionExecutor::xSUB);
    set.createInstruction("ZRO", (byte) 0x0a, InstructionDisassembler::noFlags, InstructionExecutor::xZRO);

    return set;
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_SINGLETON)
  public RegisterSet registerSet(Memory memory) throws DuplicateRegisterException {
    RegisterSet set = new RegisterSet((short) 0x000f);

    set.createRegister("ACC", memory);

    return set;
  }
}
