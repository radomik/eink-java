package radomik.com.github;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import com.pi4j.io.spi.Spi;
import com.pi4j.library.pigpio.PiGpio;
import com.pi4j.plugin.pigpio.provider.gpio.digital.PiGpioDigitalInputProvider;
import com.pi4j.plugin.pigpio.provider.gpio.digital.PiGpioDigitalOutputProvider;
import com.pi4j.plugin.pigpio.provider.spi.PiGpioSpiProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import radomik.com.github.dto.RedrawDto;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class IT8951 implements Device {
    private static final Logger LOG = LoggerFactory.getLogger(IT8951.class);

    private static final int PIN_CS = 8;
    private static final int PIN_BUSY = 24;
    private static final int PIN_RESET = 17;

    private static final byte[] TCON_SYS_RUN = {0x00, 0x01};

    private static final byte[] USDEF_I80_CMD_GET_DEV_INFO = {0x03, 0x02};
    private DigitalOutput pinOutReset;
    private DigitalOutput pinOutChipSelect;
    private Spi spi;
    private PiGpio piGpio;
    private Context pi4j;
    private DigitalInput pinInBusy;

    public IT8951(int bppMode, int vcom) throws IOException {
        spiInit();

        LOG.info("****** IT8951 ******");
        epdReset();
        getInfo();
    }

    private void getInfo() throws IOException {
        waitReadyTimeout();
        writeCmdCode(USDEF_I80_CMD_GET_DEV_INFO);

        // read 40 bytes
        int[] rawInfo = new int[20];
        readNData(rawInfo);

        int panelWidth = rawInfo[0];
        int panelHeight = rawInfo[1];
        long imageBufferAddress = (rawInfo[3] << 16) | rawInfo[2];

        LOG.info("Panel(W,H) = ({},{})", panelWidth, panelHeight);
        LOG.info("Image Buffer Address = {}", String.format("%X", imageBufferAddress));
        LOG.info("FW Version = {}", getInfoString(rawInfo, 4));
        LOG.info("LUT Version = {}", getInfoString(rawInfo, 12));

        if (panelWidth <= 0 || panelHeight <= 0 || imageBufferAddress <= 0) {
            throw new IOException("Incorrect device info");
        }
    }

    private static String getInfoString(int[] source, int sourceIndex) {
        byte[] target = new byte[16];
        int targetIndex = 0;
        while (source[sourceIndex] != 0 && targetIndex < target.length) {
            target[targetIndex++] = (byte) (source[sourceIndex] & 0xFF);
            target[targetIndex++] = (byte) ((source[sourceIndex++] >> 8) & 0xFF);
        }
        return new String(target, 0, targetIndex);
    }

    private void epdReset() throws IOException {
        LOG.info("Perform reset");
        epdReset2();
        LOG.info("Reset ok. Send SYS_RUN command...");
        writeCmdCode(TCON_SYS_RUN);
        LOG.info("Reset OK");
    }

    private void readNData(int[] target) throws IOException {
        final byte[] preamble = {0x10, 0x00};
        final byte[] dummy = {0x00, 0x00};
        waitReadyTimeout();
        pinOutChipSelect.low();
        spi.transfer(preamble);
        waitReadyTimeout();
        spi.transfer(dummy);
        waitReadyTimeout();

        LOG.info("Reading {} 2-byte words", target.length);
        for (int i = 0; i < target.length; i++) {
            byte msb = spi.readByte();
            byte lsb = spi.readByte();
            target[i] = Byte.toUnsignedInt(msb) << 8 | Byte.toUnsignedInt(lsb);
            LOG.trace("i={}, msb|lsb={}|{}, target[i]={}", i, msb, lsb, target[i]);
        }
        pinOutChipSelect.high();
    }

    private void writeCmdCode(byte[] cmdCode) throws IOException {
        final byte[] preamble = {0x60, 0x00};
        waitReadyTimeout();
        pinOutChipSelect.low();
        spi.transfer(preamble);
        waitReadyTimeout();
        spi.transfer(cmdCode);
        pinOutChipSelect.high();
    }

    private void epdReset2() {
        pinOutReset.low();
        piGpio.gpioDelayMicroseconds(100_000);
        pinOutReset.high();
        piGpio.gpioDelayMicroseconds(100_000);
    }

    private void waitReadyTimeout() throws IOException {
        waitReadyTimeout(30_000);
    }

    private void waitReadyTimeout(int timeoutMillis) throws IOException {
        int timeoutMicros = timeoutMillis * 1000;
        int stepMicros = 50;
        int totalMicros = 0;
        while (pinInBusy.isLow()) {
            try {
                TimeUnit.MICROSECONDS.sleep(stepMicros);
            } catch (InterruptedException e) {
                throw new IOException("Interrupted after total microseconds " + totalMicros, e);
            }
            totalMicros += stepMicros;
            if (totalMicros >= timeoutMicros) {
                throw new IOException("Wait ready timeout of " + timeoutMillis + " ms reached");
            }
        }
        if (totalMicros >= 5000 * 1000) {
            LOG.warn("Reached ready within {} ms", totalMicros / 1000);
        }
    }

    private void spiInit() {
        piGpio = PiGpio.newNativeInstance();
        pi4j = Pi4J.newContextBuilder()
                .noAutoDetect()
                .add(
                        PiGpioDigitalInputProvider.newInstance(piGpio),
                        PiGpioDigitalOutputProvider.newInstance(piGpio),
                        PiGpioSpiProvider.newInstance(piGpio)
                )
                .build();

        var csPinConfig = DigitalOutputConfig.newBuilder(pi4j)
                .id("IT8951-CS").name("IT8951 chip select output pin")
                .initial(DigitalState.HIGH)
                .shutdown(DigitalState.LOW)
                .address(PIN_CS);
        pinOutChipSelect = pi4j.create(csPinConfig);

        var resetPinConfig = DigitalOutputConfig.newBuilder(pi4j)
                .id("IT8951-RESET").name("IT8951 reset output pin")
                .initial(DigitalState.HIGH)
                .shutdown(DigitalState.LOW)
                .address(PIN_RESET);
        pinOutReset = pi4j.create(resetPinConfig);

        var busyPinConfig = DigitalInputConfig.newBuilder(pi4j)
                .id("IT8951-BUSY").name("IT8951 busy input pin")
                .address(PIN_BUSY);
        pinInBusy = pi4j.create(busyPinConfig);

        var spiConfig = Spi.newConfigBuilder(pi4j)
                .id("IT8951-SPI").name("IT8951 SPI bus")
                .address(1)
                .baud(250_000_000 / 16)
                .mode(0);
        spi = pi4j.create(spiConfig);
    }

    @Override
    public void redraw(RedrawDto value) throws IOException {

    }

    @Override
    public void close() {
        System.err.println("Closing IT8951");
        pinOutChipSelect.shutdown(pi4j);
        pinOutReset.shutdown(pi4j);
        pinInBusy.shutdown(pi4j);
        spi.shutdown(pi4j);
        pi4j.shutdown();
        System.err.println("Closed IT8951");
    }
}
