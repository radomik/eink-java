package radomik.com.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public abstract class CommonArgs {
    private static final Logger LOG = LoggerFactory.getLogger(CommonArgs.class);
    private BppMode bppMode;

    protected int parse(String[] args, boolean showHelp) {
        if (showHelp && CommonArgs.hasShowHelp(args)) {
            showHelp();
            System.exit(0);
            return -1;
        }
        int argno = 0;
        bppMode = BppMode.fromPixelLengthInBits(CommonArgs.getInt(args[argno++]));
        return argno;
    }

    public int parse(String[] args) {
        return parse(args, true);
    }

    public void showHelp() {
        System.out.println("Required arguments:\n" +
                "[bpp mode 4 or 8]");
    }

    public BppMode getBppMode() {
        return bppMode;
    }

    public static boolean hasShowHelp(String[] args) {
        return args.length == 0 || "--help".equalsIgnoreCase(args[0]);
    }

    public static int getInt(String arg) {
        return getInt(arg, null);
    }

    public static boolean getBoolean(String arg) {
        return getInt(arg, null) == 1;
    }

    public static int getInt(String arg, Runnable showHelpFunc) {
        try {
            return Integer.parseInt(arg);
        } catch (Exception ex) {
            LOG.warn("Fail to parse argument '{}'", arg, ex);
            if (Objects.nonNull(showHelpFunc)) {
                showHelpFunc.run();
            }
            System.exit(1);
            return -1;
        }
    }

}
