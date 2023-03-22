package radomik.com.github;

public class ClientArgs extends ClientCommonArgs {
    private String serverHost;
    private int serverPort;

    @Override
    protected int parse(String[] args, boolean showHelp) {
        if (showHelp && CommonArgs.hasShowHelp(args)) {
            showHelp();
            System.exit(0);
            return -1;
        }
        int argno = super.parse(args, false);
        serverHost = args[argno++];
        serverPort = CommonArgs.getInt(args[argno++]);
        return argno;
    }

    @Override
    public void showHelp() {
        super.showHelp();
        System.out.println("" +
                "[server host IP/address]\n" +
                "[server TCP port]");
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }
}
