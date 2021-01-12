package ca.mironov.amazon;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class DirectoryOrdersFindService implements OrdersFindService {

    private final Path directory;

    DirectoryOrdersFindService(Path directory) {
        this.directory = directory;
    }

    @Override
    public List<Path> findOrderFiles() {
            try (Stream<Path> list = Files.list(directory).filter(Files::isRegularFile)) {
                return list.map(DirectoryOrdersFindService::validatePDFExtension).sorted().collect(Collectors.toList());
            } catch (IOException e) {
                //noinspection ProhibitedExceptionThrown
                throw new RuntimeException(e);
            }
    }

    private static Path validatePDFExtension(Path file) {
        if (file.getFileName().toString().toLowerCase(Locale.getDefault()).endsWith(".pdf"))
            return file;
        else
            throw new IllegalArgumentException("unsupported invoice found: " + file);
    }

}
