package radomik.com.github.dto;

import java.io.Serializable;

public class BarInfoDto implements Serializable {
    private static final long serialVersionUID = -555492705961890304L;

    private int y0;
    private int height;

    public int getY0() {
        return y0;
    }

    public void setY0(int y0) {
        this.y0 = y0;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
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
