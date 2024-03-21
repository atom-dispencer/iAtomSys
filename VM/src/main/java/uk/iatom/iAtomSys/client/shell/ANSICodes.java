package uk.iatom.iAtomSys.client.shell;

public class ANSICodes {

    public static final String NEW_BUFFER = "\033[?1049h";
    public static final String OLD_BUFFER = "\033[?1049l";

    public static String getResetSequence() {
        return OLD_BUFFER;
    }
}
