package radomik.com.github;

import java.util.stream.Stream;

public enum BppMode {
    BIT_4(4),
    BIT_8(8);
    private final int pixelLengthInBits;
    private final int pixelCountInByte;

    BppMode(int pixelLengthInBits) {
        this.pixelLengthInBits = pixelLengthInBits;
        this.pixelCountInByte = 8 / pixelLengthInBits;
    }

    public int getPixelLengthInBits() {
        return pixelLengthInBits;
    }

    public int getPixelCountInByte() {
        return pixelCountInByte;
    }

    public static BppMode fromPixelLengthInBits(int pixelLengthInBits) {
        return Stream.of(values())
                .filter(p -> p.pixelLengthInBits == pixelLengthInBits)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported pixel length in bits " + pixelLengthInBits));
    }
}
