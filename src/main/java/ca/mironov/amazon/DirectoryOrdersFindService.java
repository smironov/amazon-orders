package ca.mironov.amazon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

class DirectoryOrdersFindService {

    private final Path directory;

    DirectoryOrdersFindService(Path directory) {
        this.directory = directory;
    }

    List<Path> findOrderFiles(String category) throws IOException {
        try (Stream<Path> list = Files.list(directory.resolve(category))) {
            return list
                    .filter(Files::isRegularFile)
                    .map(DirectoryOrdersFindService::validatePDFExtension)
                    .sorted()
                    .toList();
        }
    }

    private static Path validatePDFExtension(Path file) {
        if (file.getFileName().toString().toLowerCase(Locale.getDefault()).endsWith(".pdf"))
            return file;
        else
            throw new IllegalArgumentException("unsupported invoice found: " + file);
    }

}
