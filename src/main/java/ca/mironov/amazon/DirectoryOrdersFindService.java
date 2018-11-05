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
            try (Stream<Path> list = Files.list(directory).filter(Files::isRegularFile)) {
                return list.map(DirectoryOrdersFindService::validatePDFExtension).sorted().collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path validatePDFExtension(Path file) {
        if (file.getFileName().toString().toLowerCase().endsWith(".pdf")) {
            return file;
        } else {
            throw new IllegalArgumentException("unsupported invoice found: " + file);
        }
    }

}
