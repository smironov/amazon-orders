package ca.mironov.amazon;

import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;

import static org.junit.Assert.assertEquals;

public class KeyPDFOrderParserTest {

    @Test
    public void testParse() {
        Order order = new KeyPDFOrderParser().parse(Path.of(
                "src", "test", "resources", "Amazon.com - Order 701-7426366-1013047.pdf"));
        assertEquals(new Order(
                "701-7426366-1013047",
                LocalDate.of(2018, Month.FEBRUARY, 20),
                new BigDecimal("56.19"),
                new BigDecimal("0.00"),
                new BigDecimal("2.81"),
                new BigDecimal("59.00")
        ), order);
    }

    @Test
    public void testParseText() throws IOException {
        String text = Files.readString(Paths.get(
                "src", "test", "resources", "Amazon.com - Order 701-2903767-7852238.txt"));
        Order order = RegexPDFOrderParser.parseText(text);
        assertEquals(new Order("701-2903767-7852238", LocalDate.of(1978, Month.APRIL, 18), new BigDecimal("67.37"),
                new BigDecimal("0.00"), new BigDecimal("3.37"), new BigDecimal("70.74")), order);
    }

}
