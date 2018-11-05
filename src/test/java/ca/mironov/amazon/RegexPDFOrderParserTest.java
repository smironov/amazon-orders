package ca.mironov.amazon;

import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;

import static org.junit.Assert.assertEquals;

public class RegexPDFOrderParserTest {

    @Test
    public void testParse() {
        Order order = new RegexPDFOrderParser().parse(Path.of(
                "orders", "computers", "Amazon.com - Order 701-2903767-7852238.pdf"));
        assertEquals("701-2903767-7852238", order.getId());
    }

    @Test
    public void testParseText() throws IOException {
        String text = Files.readString(Paths.get(
                "src", "test", "resources", "Amazon.com - Order 701-2903767-7852238.txt"));
        Order order = RegexPDFOrderParser.parseText(text);
        assertEquals(new Order("701-2903767-7852238", new BigDecimal("67.37"),
                new BigDecimal("0.00"), new BigDecimal("3.37"), new BigDecimal("70.74")), order);
    }

}
