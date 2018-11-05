package ca.mironov.amazon;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.*;

public class DirectoryOrdersFindService implements OrdersFindService {

    private final Path directory;

    public DirectoryOrdersFindService(Path directory) {
        this.directory = directory;
    }

    @Override
    public List<Path> findOrderFiles() {
        try {
            try (Stream<Path> list = Files.list(directory)) {
                return list
                        .filter(path -> Files.isRegularFile(path))
                        .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".pdf"))
                        .sorted()
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
