package ca.mironov.amazon;

import java.nio.file.Path;

interface OrderParser {

    Order parse(Path file);

}
