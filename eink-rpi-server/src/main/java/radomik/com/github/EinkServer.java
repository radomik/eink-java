package radomik.com.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import radomik.com.github.dto.RedrawDto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class EinkServer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(EinkServer.class);
    private final ServerArgs serverArgs;
    private final ServerSocket serverSocket;
    private final Device device;

    public EinkServer(ServerArgs serverArgs, Device device) throws IOException {
        this.serverArgs = serverArgs;
        this.serverSocket = new ServerSocket(serverArgs.getListenPort(), 5, InetAddress.getByName(serverArgs.getListenHost()));
        this.device = device;
    }

    public static void main(String[] args) {
        ServerArgs serverArgs = new ServerArgs(args);
        try (Device device = new IT8951(serverArgs.getBppMode(), serverArgs.getVcom())) {
            new EinkServer(serverArgs, device).run();
        } catch (Exception ex) {
            LOG.debug("Server error", ex);
            ServerArgs.showHelp();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        while (true) {
            try (Socket clientSocket = serverSocket.accept();
                 ObjectInputStream stream = new ObjectInputStream(clientSocket.getInputStream())) {

                RedrawDto requestDto = (RedrawDto) stream.readObject();

                LOG.info("Received request {}", requestDto);


            } catch (Exception ex) {
                LOG.error("Fail to handle request", ex);
            }
        }
    }
}