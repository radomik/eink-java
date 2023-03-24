package radomik.com.github.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RedrawDto implements Serializable {
    private static final long serialVersionUID = -8812078606994906219L;

    private short minY;
    private short maxY;
    private byte[] pixels;
    private List<BarInfoDto> bars;
    private List<SetInfoDto> sets;

    public short getMinY() {
        return minY;
    }

    public void setMinY(short minY) {
        this.minY = minY;
    }

    public short getMaxY() {
        return maxY;
    }

    public void setMaxY(short maxY) {
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

    public List<BarInfoDto> getBars() {
        if (Objects.isNull(bars)) {
            bars = new ArrayList<>();
        }
        return bars;
    }

    public void setBars(List<BarInfoDto> bars) {
        this.bars = bars;
    }

    public List<SetInfoDto> getSets() {
        if (Objects.isNull(sets)) {
            sets = new ArrayList<>();
        }
        return sets;
    }

    public void setSets(List<SetInfoDto> sets) {
        this.sets = sets;
    }

    @Override
    public String toString() {
        return "RedrawDto{" +
                "minY=" + minY +
                ", maxY=" + maxY +
                ", pixels.length=" + getPixels().length +
                ", bars.length=" + getBars().size() +
                ", sets.length=" + getSets().size() +
                '}';
    }
}
