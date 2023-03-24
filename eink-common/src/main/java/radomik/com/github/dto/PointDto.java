package radomik.com.github.dto;

import java.io.Serializable;

public class PointDto implements Serializable {
    private static final long serialVersionUID = 1384899799243658211L;

    private short x;
    private short y;

    public short getX() {
        return x;
    }

    public void setX(short x) {
        this.x = x;
    }

    public short getY() {
        return y;
    }

    public void setY(short y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
