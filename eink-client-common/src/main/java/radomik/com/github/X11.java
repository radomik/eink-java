package radomik.com.github;

import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import radomik.com.github.dto.RedrawDto;
import radomik.com.github.dto.SetInfoDto;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class X11 {

    private static final Logger LOG = LoggerFactory.getLogger(X11.class);

    private final ClientCommonArgs args;
    private final Device device;
    private final Rectangle screenshotArea;
    private final RedrawDto redrawDto = new RedrawDto();
    private Robot robot;

    private byte[] prevPixels;
    private byte[] nextPixels;

    private final byte[] nextPixelsCv;
    private short minY, maxY;

    private Mat prevImg;

    static {
        OpenCV.loadLocally();
    }

    public X11(ClientCommonArgs args, Device device) {
        this.args = args;
        this.device = device;

        this.screenshotArea = new Rectangle(
                args.getScreenX0(), args.getScreenY0(),
                args.getScreenWidth(), args.getScreenHeight());

        prevPixels = new byte[args.getScreenWidth() * args.getScreenHeight() / args.getBppMode().getPixelCountInByte()];
        nextPixels = new byte[prevPixels.length];
        nextPixelsCv = new byte[args.getScreenWidth() * args.getScreenHeight()];
        redrawDto.setSets(new ArrayList<>(args.getSetSize()));
    }

    public void redraw() throws IOException {
        BufferedImage sourcePixels = takeScreenshot();

        boolean changed = convert(sourcePixels);

        if (!changed) {
            return;
        }

        redrawDto.getSets().clear();

        Mat nextImg = new Mat(screenshotArea.height, screenshotArea.width, CvType.CV_8UC1);
        nextImg.put(0, 0, nextPixelsCv);
        if (prevImg != null) { // run opencv set extraction
            Mat diffImg = new Mat(screenshotArea.height, screenshotArea.width, CvType.CV_8UC1);
            Core.absdiff(prevImg, nextImg, diffImg);

            List<MatOfPoint> contours = new ArrayList<>();

            Imgproc.findContours(diffImg, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            if (contours.size() <= args.getSetSize()) {
                for (int i = 0; i < contours.size(); i++) {
                    Rect rect = Imgproc.boundingRect(contours.get(i));
                    SetInfoDto set = new SetInfoDto();
                    redrawDto.getSets().add(set);
                    set.getMin().setX((short) rect.x);
                    set.getMin().setY((short) rect.y);
                    set.getMax().setX((short) (rect.x + rect.width - 1));
                    set.getMax().setY((short) (rect.y + rect.height - 1));
                    System.err.printf("Set[%d] min(%d,%d) max(%d,%d)\n", i,
                            set.getMin().getX(), set.getMin().getY(),
                            set.getMax().getX(), set.getMax().getY());
                }
            } else {
                System.err.printf("Too many sets: %d\n", contours.size());
//                HighGui.imshow("prevImg", prevImg);
//                HighGui.imshow("nextImg", nextImg);
//                HighGui.imshow("diffImg " + contours.size(), diffImg);
//                HighGui.waitKey();
            }
        }

        prevImg = nextImg;

        redrawDto.setPixels(nextPixels);
        redrawDto.setMinY(minY);
        redrawDto.setMaxY(maxY);

        byte[] temp = nextPixels;
        nextPixels = prevPixels;
        prevPixels = temp;

        try {
            device.redraw(redrawDto);
        } catch (IOException ex) {
            LOG.error("Device redraw failed with {}", redrawDto, ex);
        }
    }

    private boolean convert(BufferedImage image) {
        int[] sourcePixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        int targetOffset = 0;
        int targetCvOffset = 0;
        int sourceOffset = 0;
        short x = 0;
        short y = 0;
        boolean anyPixelChanged = false;
        minY = -1;
        maxY = 0;
        if (args.getBppMode() == BppMode.BIT_4) {
            while (targetOffset < nextPixels.length) {
                int msbPixel = getGrayByte(sourcePixels, sourceOffset, image.getColorModel());
                nextPixelsCv[targetCvOffset++] = (byte) (msbPixel & 0xF0);
                sourceOffset++;

                int lsbPixel = getGrayByte(sourcePixels, sourceOffset, image.getColorModel());
                nextPixelsCv[targetCvOffset++] = (byte) (lsbPixel & 0xF0);
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
                x += 2;
                if (x >= screenshotArea.width) {
                    x = 0;
                    y++;
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
                nextPixelsCv[targetCvOffset++] = grey;

                if (currentPixelChanged && minY < 0) {
                    minY = y;
                }
                if (currentPixelChanged && y > maxY) {
                    maxY = y;
                }

                targetOffset++;
                x++;
                if (x >= screenshotArea.width) {
                    x = 0;
                    y++;
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
