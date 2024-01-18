package ca.mironov.amazon;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

interface OrdersFindService {

    List<Path> findOrderFiles() throws IOException;

}
