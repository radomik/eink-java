package radomik.com.github;

import radomik.com.github.dto.BarInfoDto;
import radomik.com.github.dto.RedrawDto;
import radomik.com.github.dto.SetInfoDto;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpClient implements Device {

    private final InetAddress serverHost;
    private final int serverPort;

    public TcpClient(String serverHost, int serverPort) throws UnknownHostException {
        this.serverHost = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;
    }

    @Override
    public void redraw(RedrawDto value) throws IOException {
        try (Socket socket = new Socket(serverHost, serverPort)) {
            try (DataOutputStream stream = new DataOutputStream(socket.getOutputStream())) {
                System.err.printf("Send    : px_bytesize=%d, bar,set_count=%d,%d, min,maxy=%d,%d, %02X,%02X,%02X,%02X,%02X,...,%02X,%02X,%02X,%02X,%02X\n",
                        value.getPixels().length, value.getBars().size(), value.getSets().size(),
                        value.getMinY(), value.getMaxY(),
                        value.getPixels()[0],
                        value.getPixels()[1],
                        value.getPixels()[2],
                        value.getPixels()[3],
                        value.getPixels()[4],

                        value.getPixels()[value.getPixels().length-5],
                        value.getPixels()[value.getPixels().length-4],
                        value.getPixels()[value.getPixels().length-3],
                        value.getPixels()[value.getPixels().length-2],
                        value.getPixels()[value.getPixels().length-1]
                );
                stream.writeShort(Short.reverseBytes(value.getMinY()));
                stream.writeShort(Short.reverseBytes(value.getMaxY()));
                stream.writeInt(Integer.reverseBytes(value.getPixels().length));
                stream.writeShort(Short.reverseBytes((short) value.getBars().size()));
                stream.writeShort(Short.reverseBytes((short) value.getSets().size()));
                stream.write(value.getPixels());
                for (BarInfoDto bar : value.getBars()) {
                    stream.writeShort(Short.reverseBytes(bar.getY0()));
                    stream.writeShort(Short.reverseBytes(bar.getHeight()));
                }
                for (SetInfoDto set : value.getSets()) {
                    stream.writeShort(Short.reverseBytes(set.getMin().getX()));
                    stream.writeShort(Short.reverseBytes(set.getMin().getY()));
                    stream.writeShort(Short.reverseBytes(set.getMax().getX()));
                    stream.writeShort(Short.reverseBytes(set.getMax().getY()));
                }
            }
        }
    }

    @Override
    public void close() {
        // nothing to close
    }
}
