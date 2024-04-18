package uk.iatom.iAtomSys.client.configuration;

import org.jline.utils.AttributedString;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

@Configuration
public class ShellPromptConfiguration implements PromptProvider {

  @Override
  public AttributedString getPrompt() {
    return new AttributedString("");  // Remove the prompt
  }
}
