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
                "701-7426366-1013047", LocalDate.of(2018, Month.FEBRUARY, 20),
                new BigDecimal("56.19"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("56.19"),
                new BigDecimal("2.81"),
                new BigDecimal("59.00"),
                "The Choice, Goldratt, Eliyahu M; The Haystack Syndrome: Sifting Information Out of the Data Ocean, Goldratt,"
        ), order);
    }

}
