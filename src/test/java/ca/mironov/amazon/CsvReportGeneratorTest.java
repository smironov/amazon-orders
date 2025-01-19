package ca.mironov.amazon;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CsvReportGeneratorTest {

    private Path tempDirectory;

    @Before
    public void setUp() throws Exception {
        tempDirectory = Files.createTempDirectory("amazon-orders");
    }

    @Test
    public void test() throws Exception {
        ReportGenerator reportGenerator = new CsvReportGenerator(tempDirectory);
        reportGenerator.generate("junit", List.of(
                new Order("junit", "101", LocalDate.of(2020, Month.DECEMBER, 31),
                        new BigDecimal("123.01"), new BigDecimal("3.99"), new BigDecimal("2.00"), new BigDecimal("0.00"), new BigDecimal("125.00"), new BigDecimal("0.00"), new BigDecimal("15.99"), new BigDecimal("0.00"), new BigDecimal("140.99"), "Item 1 long description"),
                new Order("junit", "102", LocalDate.of(2020, Month.JANUARY, 12), new BigDecimal("100.00"), new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("100.00"), new BigDecimal("0.00"), new BigDecimal("5.00"), new BigDecimal("8.00"), new BigDecimal("113.00"), "Item 2")
        ));
        assertThat(Files.readAllLines(tempDirectory.resolve("junit.csv")), is(List.of(
                "Date,Order #,Items subtotal with discount,Shipping & handling,Environmental handling fee,Total before tax,HST,Total,Items",
                "2020-12-31,101,121.01,3.99,0.00,125.00,15.99,140.99,Item 1 long description",
                "2020-01-12,102,100.00,0.00,0.00,100.00,13.00,113.00,Item 2",
                "",
                ",Total:,221.01,3.99,0.00,225.00,28.99,253.99"
        )));
    }

    @After
    public void tearDown() throws Exception {
        try (Stream<Path> files = Files.list(tempDirectory)) {
            files.forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    //noinspection ProhibitedExceptionThrown
                    throw new RuntimeException(e);
                }
            });
        }
        Files.delete(tempDirectory);
        LoggerFactory.getLogger(CsvReportGeneratorTest.class).debug("Deleted generated files");
    }

}
