package uk.iatom.iAtomSys.server;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.Setter;

@Getter
public class AsyncRunData {

  private final AtomicLong asyncExecutedInstructions = new AtomicLong(0L);

  @Setter
  private LocalDateTime startTime = LocalDateTime.now();
}
