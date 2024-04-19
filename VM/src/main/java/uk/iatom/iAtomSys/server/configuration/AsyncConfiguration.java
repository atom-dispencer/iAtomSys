package uk.iatom.iAtomSys.server.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uk.iatom.iAtomSys.server.IAtomSysVM;

@Configuration
@EnableScheduling
public class AsyncConfiguration {

  @Autowired
  private VMConfiguration vmConfiguration;

  @Autowired
  private IAtomSysVM iAtomSysVM;

  @Scheduled(fixedRateString = "#{@vmConfiguration.millisPerCycle}")
  public void processNextCycle() {
    if (vmConfiguration.running) {
      iAtomSysVM.processNextCycle();
    }
  }
}
