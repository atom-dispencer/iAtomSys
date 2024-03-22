package uk.iatom.iAtomSys.client.shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.DefaultShellApplicationRunner;
import org.springframework.shell.ShellRunner;
import org.springframework.shell.jline.InteractiveShellRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Not currently used. See {@link uk.iatom.iAtomSys.client.shell.configuration.ShellBeans}
 * Causes an unknown issue requiring additional Ctrl+C input after application context has 'closed'.
 */
@Deprecated
@Component
public class WrappedInteractiveShellApplicationRunner extends DefaultShellApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(WrappedInteractiveShellApplicationRunner.class);

    @Autowired
    InteractiveShellRunner interactiveShellRunner;

    public WrappedInteractiveShellApplicationRunner(List<ShellRunner> shellRunners) {
        super(shellRunners);
    }
}
