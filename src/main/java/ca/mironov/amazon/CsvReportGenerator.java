package ca.mironov.amazon;

import org.apache.commons.csv.*;

import java.io.*;
import java.math.BigDecimal;
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
                csvPrinter.printRecord("Order #", "Items subtotal", "Shipping & handling", "Environmental handling fee", "Total before tax", "HST", "Total", "Items");
                BigDecimal totalItemsSubtotal = BigDecimal.ZERO;
                BigDecimal totalShippingAndHandling = BigDecimal.ZERO;
                BigDecimal totalEnvironmentalHandlingFee = BigDecimal.ZERO;
                BigDecimal totalTotalBeforeTax = BigDecimal.ZERO;
                BigDecimal totalHst = BigDecimal.ZERO;
                BigDecimal totalTotal = BigDecimal.ZERO;
                for (Order order : orders) {
                    csvPrinter.printRecord(order.getId(),
                            order.getItemsSubtotal(), order.getShippingAndHandling(), order.getEnvironmentalHandlingFee(),
                            order.getTotalBeforeTax(), order.getHst(), order.getTotal(), order.getItems());
                    totalItemsSubtotal = totalItemsSubtotal.add(order.getItemsSubtotal());
                    totalShippingAndHandling = totalShippingAndHandling.add(order.getShippingAndHandling());
                    totalEnvironmentalHandlingFee = totalEnvironmentalHandlingFee.add(order.getEnvironmentalHandlingFee());
                    totalTotalBeforeTax = totalTotalBeforeTax.add(order.getTotalBeforeTax());
                    totalHst = totalHst.add(order.getHst());
                    totalTotal = totalTotal.add(order.getTotal());
                }
                // print total
                csvPrinter.printRecord();
                csvPrinter.printRecord(
                        "Total:", totalItemsSubtotal, totalShippingAndHandling, totalEnvironmentalHandlingFee,
                        totalTotalBeforeTax, totalHst, totalTotal
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
