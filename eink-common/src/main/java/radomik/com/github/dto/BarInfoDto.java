package radomik.com.github.dto;

import java.io.Serializable;

public class BarInfoDto implements Serializable {
    private static final long serialVersionUID = -555492705961890304L;

    private short y0;
    private short height;

    public short getY0() {
        return y0;
    }

    public void setY0(short y0) {
        this.y0 = y0;
    }

    public short getHeight() {
        return height;
    }

    public void setHeight(short height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "BarInfoDto{" +
                "y0=" + y0 +
                ", height=" + height +
                '}';
    }
}
