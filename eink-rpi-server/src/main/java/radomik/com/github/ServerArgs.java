package radomik.com.github;

public class ServerArgs {
    private int bppMode;
    private int vcom;
    private String listenHost;
    private int listenPort;

    public ServerArgs(String[] args) {
        if (CommonArgs.hasShowHelp(args)) {
            showHelp();
            System.exit(0);
        }
        bppMode = CommonArgs.getInt(args[0], ServerArgs::showHelp);
        vcom = CommonArgs.getInt(args[1], ServerArgs::showHelp);
        listenHost = args[2];
        listenPort = CommonArgs.getInt(args[3], ServerArgs::showHelp);
    }

    public static void showHelp() {
        System.out.println("Required arguments:\n" +
                " [bpp mode 4 or 8]\n" +
                " [vcom in mV, 0 to unchange]\n" +
                " [listen host IP/addresss]\n" +
                " [listen TCP port]");
    }

    public int getBppMode() {
        return bppMode;
    }

    public int getVcom() {
        return vcom;
    }

    public String getListenHost() {
        return listenHost;
    }

    public int getListenPort() {
        return listenPort;
    }
}
