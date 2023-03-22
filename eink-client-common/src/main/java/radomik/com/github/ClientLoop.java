package radomik.com.github;

import java.io.IOException;

public class ClientLoop {
    private final ClientCommonArgs args;
    private final X11 x11;

    public ClientLoop(ClientCommonArgs args, Device device) {
        this.args = args;
        this.x11 = new X11(args, device);
    }

    public void run() throws InterruptedException, IOException {
        while (true) {
            Thread.sleep(5);
            x11.redraw();
        }
    }
}
