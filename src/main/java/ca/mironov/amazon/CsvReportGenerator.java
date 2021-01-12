package ca.mironov.amazon;

import org.apache.commons.csv.*;

import java.io.IOException;
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
            Files.createDirectories(directory);
            Path reportFile = directory.resolve(reportName + ".csv");
            try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(reportFile), CSVFormat.EXCEL)) {
                // print headers
                csvPrinter.printRecord("Date", "Order #",
                        "Items subtotal with discount", "Shipping & handling", "Environmental handling fee", "Total before tax", "HST", "Total", "Items");
                BigDecimal totalItemsSubtotalWithDiscount = Order.FINANCIAL_ZERO;
                BigDecimal totalShippingAndHandling = Order.FINANCIAL_ZERO;
                BigDecimal totalEnvironmentalHandlingFee = Order.FINANCIAL_ZERO;
                BigDecimal totalTotalBeforeTax = Order.FINANCIAL_ZERO;
                BigDecimal totalHst = Order.FINANCIAL_ZERO;
                BigDecimal totalTotal = Order.FINANCIAL_ZERO;
                for (Order order : orders) {
                    csvPrinter.printRecord(order.getDate(), order.getId(),
                            order.getItemsSubtotal().subtract(order.getDiscount()), order.getShippingAndHandling(), order.getEnvironmentalHandlingFee(),
                            order.getTotalBeforeTax(), order.getHst(), order.getTotal(), order.getItems());
                    totalItemsSubtotalWithDiscount = totalItemsSubtotalWithDiscount.add(order.getItemsSubtotal()).subtract(order.getDiscount());
                    totalShippingAndHandling = totalShippingAndHandling.add(order.getShippingAndHandling());
                    totalEnvironmentalHandlingFee = totalEnvironmentalHandlingFee.add(order.getEnvironmentalHandlingFee());
                    totalTotalBeforeTax = totalTotalBeforeTax.add(order.getTotalBeforeTax());
                    totalHst = totalHst.add(order.getHst());
                    totalTotal = totalTotal.add(order.getTotal());
                }
                // print total
                csvPrinter.printRecord();
                csvPrinter.printRecord(
                        null, "Total:", totalItemsSubtotalWithDiscount, totalShippingAndHandling, totalEnvironmentalHandlingFee,
                        totalTotalBeforeTax, totalHst, totalTotal
                );
            }
        } catch (IOException e) {
            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException(e);
        }
    }

}
