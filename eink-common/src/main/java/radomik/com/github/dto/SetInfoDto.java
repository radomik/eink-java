package radomik.com.github.dto;

import java.io.Serializable;
import java.util.Objects;

public class SetInfoDto implements Serializable {
    private static final long serialVersionUID = 8388483409800020339L;
    private PointDto min;
    private PointDto max;

    public PointDto getMin() {
        if (Objects.isNull(min)) {
            min = new PointDto();
        }
        return min;
    }

    public void setMin(PointDto min) {
        this.min = min;
    }

    public PointDto getMax() {
        if (Objects.isNull(max)) {
            max = new PointDto();
        }
        return max;
    }

    public void setMax(PointDto max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "SetInfoDto{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }
}
