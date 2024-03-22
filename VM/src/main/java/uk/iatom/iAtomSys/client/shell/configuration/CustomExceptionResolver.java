package uk.iatom.iAtomSys.client.shell.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.shell.command.CommandExceptionResolver;
import org.springframework.shell.command.CommandHandlingResult;
import org.springframework.stereotype.Component;
import uk.iatom.iAtomSys.client.shell.ShellDisplay;

@Component
public class CustomExceptionResolver implements CommandExceptionResolver {

    @Autowired
    ShellDisplay shellDisplay;

    private final Logger logger = LoggerFactory.getLogger(CustomExceptionResolver.class);

    @Override
    public CommandHandlingResult resolve(Exception ex) {
        logger.error("Error during command execution.", ex);

        String message;
        if (ex instanceof ConversionFailedException cfe) {
            TypeDescriptor sourceTypeDescriptor = cfe.getSourceType();
            TypeDescriptor targetTypeDescriptor = cfe.getTargetType();

            message = "Error: Can't convert %s to %s".formatted(
                    sourceTypeDescriptor != null ? sourceTypeDescriptor.getType().getSimpleName() : "<null>",
                    targetTypeDescriptor.getType().getSimpleName()
            );
        } else {
            message = "Error logged: %s".formatted(ex.getClass().getSimpleName());
        }

        shellDisplay.getState().setCommandMessage(message);
        shellDisplay.draw();
        return CommandHandlingResult.empty();
    }

}
