package uk.iatom.iAtomSys.client.shell;

import lombok.Getter;
import lombok.Setter;

public class ShellDisplay {

    @Setter
    @Getter
    private boolean alive;

    private void assertShellLive() {
        assert alive : new IllegalStateException("ShellDisplay cannot take actions while the shell is not live.");
    }

    public void showShortMessage(String shortMessage) {
        assertShellLive();
        String messageLengthEnforced = shortMessage.substring(0, Math.min(64, shortMessage.length()));
        System.out.println("ShellDisplay : " + messageLengthEnforced);
    }
}
