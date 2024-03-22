package uk.iatom.iAtomSys.server.vm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IAtomSysVM {

  private Logger logger = LoggerFactory.getLogger(IAtomSysVM.class);

  public IAtomSysVM() {
  }

  public void processNextCycle() {

    //logger.info("Processing!");
  }
}
