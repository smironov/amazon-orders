package ca.mironov.amazon;

import org.apache.commons.csv.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public class CsvReportGenerator implements ReportGenerator {

    private final Path directory;

    CsvReportGenerator(Path directory) {
        this.directory = directory;
    }

    @Override
    public void generate(String reportName, List<? extends Order> orders) {
        try {
            if (Files.notExists(directory)) {
                Files.createDirectories(directory);
            }
            Path reportFile = directory.resolve(reportName + ".csv");
            try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(reportFile), CSVFormat.EXCEL)) {
                // print headers
                csvPrinter.printRecord("Order #", "Total before tax", "Shipping & handling", "HST", "Total");
                for (Order order : orders) {
                    csvPrinter.printRecord(order.getId(), order.getTotalBeforeTax(), order.getShippingAndHandling(),
                            order.getHst(), order.getTotal());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
