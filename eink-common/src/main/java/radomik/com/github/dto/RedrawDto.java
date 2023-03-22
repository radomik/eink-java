package radomik.com.github.dto;

import java.io.Serializable;
import java.util.Objects;

public class RedrawDto implements Serializable {
    private static final long serialVersionUID = -8812078606994906219L;

    private int minY;
    private int maxY;
    private byte[] pixels;

    private BarInfoDto[] bars;
    private SetInfoDto[] sets;

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public byte[] getPixels() {
        if (Objects.isNull(pixels)) {
            pixels = new byte[0];
        }
        return pixels;
    }

    public void setPixels(byte[] pixels) {
        this.pixels = pixels;
    }

    public BarInfoDto[] getBars() {
        if (Objects.isNull(bars)) {
            bars = new BarInfoDto[0];
        }
        return bars;
    }

    public void setBars(BarInfoDto[] bars) {
        this.bars = bars;
    }

    public SetInfoDto[] getSets() {
        if (Objects.isNull(sets)) {
            sets = new SetInfoDto[0];
        }
        return sets;
    }

    public void setSets(SetInfoDto[] sets) {
        this.sets = sets;
    }

    @Override
    public String toString() {
        return "RedrawDto{" +
                "minY=" + minY +
                ", maxY=" + maxY +
                ", pixels.length=" + getPixels().length +
                ", bars.length=" + getBars().length +
                ", sets.length=" + getSets().length +
                '}';
    }
}
