package radomik.com.github;

import radomik.com.github.dto.RedrawDto;

import java.io.Closeable;
import java.io.IOException;

public interface Device extends Closeable {
    void redraw(RedrawDto value) throws IOException;
}
