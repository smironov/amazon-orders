package ca.mironov.amazon;

import java.nio.file.Path;

public interface OrderParser {

    Order parse(Path file);

}
