package ca.mironov.amazon;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class CsvReportGenerator implements ReportGenerator {

    private final Path directory;

    CsvReportGenerator(Path directory) {
        this.directory = directory;
    }

    @Override
    public void generate(String reportName, List<Order> orders) throws IOException {
        Path reportFile = Files.createDirectories(directory).resolve(reportName + ".csv");
        //noinspection NestedTryStatement
        try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(reportFile), CSVFormat.EXCEL)) {
            // print headers
            csvPrinter.printRecord("Date", "Order #",
                    "Items subtotal with discount", "Shipping & handling", "Environmental handling fee", "Total before tax", "HST", "Total", "Items");
            BigDecimal totalItemsSubtotalWithDiscount = Order.FINANCIAL_ZERO;
            BigDecimal totalShippingAndHandling = Order.FINANCIAL_ZERO;
            BigDecimal totalEnvironmentalHandlingFee = Order.FINANCIAL_ZERO;
            BigDecimal totalTotalBeforeTax = Order.FINANCIAL_ZERO;
            BigDecimal totalTax = Order.FINANCIAL_ZERO;
            BigDecimal totalTotal = Order.FINANCIAL_ZERO;
            for (Order order : orders) {
                BigDecimal combinedTax = order.hst().add(order.qst());
                csvPrinter.printRecord(order.date(), order.id(),
                        order.itemsSubtotal().subtract(order.discount()), order.shippingAndHandling(), order.environmentalHandlingFee(),
                        order.totalBeforeTax(), combinedTax, order.total(), order.items());
                totalItemsSubtotalWithDiscount = totalItemsSubtotalWithDiscount.add(order.itemsSubtotal()).subtract(order.discount());
                totalShippingAndHandling = totalShippingAndHandling.add(order.shippingAndHandling());
                totalEnvironmentalHandlingFee = totalEnvironmentalHandlingFee.add(order.environmentalHandlingFee());
                totalTotalBeforeTax = totalTotalBeforeTax.add(order.totalBeforeTax());
                totalTax = totalTax.add(combinedTax);
                totalTotal = totalTotal.add(order.total());
            }
            // print total
            csvPrinter.printRecord();
            csvPrinter.printRecord(
                    null, "Total:", totalItemsSubtotalWithDiscount, totalShippingAndHandling, totalEnvironmentalHandlingFee,
                    totalTotalBeforeTax, totalTax, totalTotal
            );
        }
    }

}
