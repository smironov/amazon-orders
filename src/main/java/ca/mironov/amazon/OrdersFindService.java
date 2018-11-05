package ca.mironov.amazon;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface OrdersFindService {

    List<Path> findOrderFiles();

}
