package uk.iatom.iAtomSys.server.vm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.iatom.iAtomSys.server.vm.memory.Memory;
import uk.iatom.iAtomSys.server.vm.register.RegisterSet;
import uk.iatom.iAtomSys.server.vm.stack.ProcessorStack;

@Component
public class IAtomSysVM {

  private Logger logger = LoggerFactory.getLogger(IAtomSysVM.class);

  @Autowired
  private Memory memory;

  @Autowired
  private RegisterSet registerSet;

  @Autowired
  private ProcessorStack processorStack;

  public void processNextCycle() {

    logger.info("Processing!");
  }
}
