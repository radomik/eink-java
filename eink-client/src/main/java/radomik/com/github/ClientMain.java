package radomik.com.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientMain {
    private static final Logger LOG = LoggerFactory.getLogger(ClientMain.class);

    public static void main(String[] cmdline) {
        ClientArgs args = new ClientArgs();
        try {
            args.parse(cmdline);
            TcpClient tcpClient = new TcpClient(args.getServerHost(), args.getServerPort());
            ClientLoop clientLoop = new ClientLoop(args, tcpClient);
            clientLoop.run();
        } catch (Exception ex) {
            LOG.error("Fail to run client", ex);
            System.exit(1);
        }
    }
}