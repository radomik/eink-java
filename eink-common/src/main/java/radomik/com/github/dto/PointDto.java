package radomik.com.github.dto;

import java.io.Serializable;

public class PointDto implements Serializable {
    private static final long serialVersionUID = 1384899799243658211L;

    private int x;
    private int y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
