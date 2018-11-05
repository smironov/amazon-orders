package ca.mironov.amazon;

import java.io.IOException;
import java.nio.file.Path;

public interface OrderParser {

    Order parse(Path file) throws IOException;

}
