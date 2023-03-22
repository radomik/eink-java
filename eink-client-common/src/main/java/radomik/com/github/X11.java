package radomik.com.github;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import radomik.com.github.dto.RedrawDto;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.io.IOException;

public class X11 {

    private static final Logger LOG = LoggerFactory.getLogger(X11.class);

    private final ClientCommonArgs args;
    private final Device device;
    private final Rectangle screenshotArea;
    private final RedrawDto redrawDto = new RedrawDto();
    private Robot robot;

    private byte[] prevPixels;
    private byte[] nextPixels;
    private short minY, maxY;

    private Mat prevImg;
    private Mat nextImg;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public X11(ClientCommonArgs args, Device device) {
        this.args = args;
        this.device = device;

        this.screenshotArea = new Rectangle(
                args.getScreenX0(), args.getScreenY0(),
                args.getScreenWidth(), args.getScreenHeight());

        prevPixels = new byte[args.getScreenWidth() * args.getScreenHeight() / args.getBppMode().getPixelCountInByte()];
        nextPixels = new byte[prevPixels.length];
        redrawDto.setMinY(0);
        redrawDto.setMaxY(args.getScreenHeight() - 1);
    }

    public void redraw() throws IOException {
        BufferedImage sourcePixels = takeScreenshot();

        boolean changed = convert(sourcePixels);

        if (!changed) {
            return;
        }

        nextImg = new Mat(screenshotArea.height, screenshotArea.width, CvType.CV_8UC1);
        nextImg.put(0, 0, nextPixels);
        if (prevImg != null) { // run opencv set extraction
            Mat diffImg = new Mat(screenshotArea.height, screenshotArea.width, CvType.CV_8UC1);
            Core.absdiff(prevImg, nextImg, diffImg);
            HighGui.imshow("prevImg", prevImg);
            HighGui.imshow("nextImg", nextImg);
            HighGui.imshow("diffImg", diffImg);
            HighGui.waitKey();
        }

        prevImg = nextImg;

        redrawDto.setPixels(nextPixels);
        redrawDto.setMinY(minY);
        redrawDto.setMaxY(maxY);

        byte[] temp = nextPixels;
        nextPixels = prevPixels;
        prevPixels = temp;

        //todo: add bars/sets redraw and limit miny/maxy
        try {
            device.redraw(redrawDto);
        } catch (IOException ex) {
            LOG.error("Device redraw failed with {}", redrawDto, ex);
        }
    }

    private boolean convert(BufferedImage image) {
        int[] sourcePixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        int targetOffset = 0;
        int sourceOffset = 0;
        short x = 0;
        short y = 0;
        boolean anyPixelChanged = false;
        minY = -1;
        maxY = 0;
        if (args.getBppMode() == BppMode.BIT_4) {
            while (targetOffset < nextPixels.length) {
                int msbPixel = getGrayByte(sourcePixels, sourceOffset, image.getColorModel());
                sourceOffset++;

                int lsbPixel = getGrayByte(sourcePixels, sourceOffset, image.getColorModel());
                sourceOffset++;

                byte grey = (byte) ((msbPixel & 0xF0) | (lsbPixel >> 4));

                boolean currentPixelChanged = grey != prevPixels[targetOffset];
                anyPixelChanged |= currentPixelChanged;
                nextPixels[targetOffset] = grey;

                if (currentPixelChanged && minY < 0) {
                    minY = y;
                }
                if (currentPixelChanged && y > maxY) {
                    maxY = y;
                }

                targetOffset++;
                y += 2;
                if (y >= screenshotArea.width) {
                    y = 0;
                    x++;
                }
            }
            return anyPixelChanged;
        }
        if (args.getBppMode() == BppMode.BIT_8) {
            while (targetOffset < nextPixels.length) {
                byte grey = (byte) getGrayByte(sourcePixels, sourceOffset, image.getColorModel());
                sourceOffset++;

                boolean currentPixelChanged = grey != prevPixels[targetOffset];
                anyPixelChanged |= currentPixelChanged;
                nextPixels[targetOffset] = grey;

                if (currentPixelChanged && minY < 0) {
                    minY = y;
                }
                if (currentPixelChanged && y > maxY) {
                    maxY = y;
                }

                targetOffset++;
                y++;
                if (y >= screenshotArea.width) {
                    y = 0;
                    x++;
                }
            }
            return anyPixelChanged;
        }
        throw new IllegalArgumentException("Unsupported bpp mode " + args.getBppMode());
    }

    private BufferedImage takeScreenshot() throws IOException {
        boolean initialized = robot != null;
        if (!initialized) {
            initRobot();
        }
        return robot.createScreenCapture(screenshotArea);
    }

    private int getGrayByte(int[] sourcePixels, int sourceOffset, ColorModel colorModel) {
        int pixel = sourcePixels[sourceOffset];
        int blue = colorModel.getBlue(pixel);
        int green = colorModel.getGreen(pixel);
        int red = colorModel.getRed(pixel);
        if (args.isInvertColors()) {
            blue = 255 - blue;
            green = 255 - green;
            red = 255 - red;
        }

        return ((red * 299 + green * 587 + blue * 114) / 1000) & 0xFF;
    }

    private void initRobot() throws IOException {
        try {
            this.robot = new Robot();
        } catch (AWTException e) {
            throw new IOException("Fail to create AWT Robot", e);
        }
    }
}
