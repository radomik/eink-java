package radomik.com.github;

public abstract class ClientCommonArgs extends CommonArgs {

    private int setSize;
    private int setMaxDist;
    private int screenWidth;
    private int screenHeight;
    private int screenX0;
    private int screenY0;
    private int barHeight;
    private int nFullRefresh;
    private boolean invertColors;

    @Override
    protected int parse(String[] args, boolean showHelp) {
        if (showHelp && CommonArgs.hasShowHelp(args)) {
            showHelp();
            System.exit(0);
            return -1;
        }
        int argno = super.parse(args, false);
        barHeight = CommonArgs.getInt(args[argno++]);
        nFullRefresh = CommonArgs.getInt(args[argno++]);
        setSize = CommonArgs.getInt(args[argno++]);
        setMaxDist = CommonArgs.getInt(args[argno++]);
        screenWidth = CommonArgs.getInt(args[argno++]);
        screenHeight = CommonArgs.getInt(args[argno++]);
        screenX0 = CommonArgs.getInt(args[argno++]);
        screenY0 = CommonArgs.getInt(args[argno++]);
        invertColors = CommonArgs.getBoolean(args[argno++]);
        return argno;
    }


    @Override
    public void showHelp() {
        super.showHelp();
        System.out.println(
                "[bar height]\n" +
                        "[full refresh every N frames]\n" +
                        "[set max count]\n" +
                        "[max dist for same set]\n" +
                        "[screen width]\n" +
                        "[screen height]\n" +
                        "[screen x0]\n" +
                        "[screen y0]\n" +
                        "[invert colors: 0 or 1]");
    }


    public int getSetSize() {
        return setSize;
    }

    public int getSetMaxDist() {
        return setMaxDist;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getScreenX0() {
        return screenX0;
    }

    public int getScreenY0() {
        return screenY0;
    }

    public int getBarHeight() {
        return barHeight;
    }

    public int getnFullRefresh() {
        return nFullRefresh;
    }

    public boolean isInvertColors() {
        return invertColors;
    }
}
