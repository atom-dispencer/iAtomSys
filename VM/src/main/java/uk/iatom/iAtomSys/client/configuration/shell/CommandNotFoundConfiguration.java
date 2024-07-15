package uk.iatom.iAtomSys.client.configuration.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.result.CommandNotFoundMessageProvider;
import org.springframework.stereotype.Component;
import uk.iatom.iAtomSys.client.shell.ShellDisplay;

@Component
public class CommandNotFoundConfiguration implements CommandNotFoundMessageProvider {

  @Autowired
  ShellDisplay shellDisplay;

  @Override
  public String apply(ProviderContext providerContext) {

    String message = "Command not found: '%s'".formatted(providerContext.text());
    String trimmed = message.substring(0, Math.min(64, message.length()));
    shellDisplay.getDisplayState().setCommandMessage(trimmed);
    shellDisplay.draw(true);
    return "";
  }
}
