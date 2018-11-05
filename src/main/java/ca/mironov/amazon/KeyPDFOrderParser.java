package ca.mironov.amazon;

import com.google.common.collect.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.*;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class KeyPDFOrderParser implements OrderParser {

    private static final Logger logger = LoggerFactory.getLogger(KeyPDFOrderParser.class);

    @Override
    public Order parse(Path file) {
        logger.debug("parsing file: {}", file);
        try (InputStream in = Files.newInputStream(file)) {
            try (PDDocument document = PDDocument.load(in)) {
                if (document.isEncrypted()) {
                    throw new IllegalArgumentException("document is encrypted: " + file);
                }
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                return parseText(text);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Pattern[] PATTERNS = {
            Pattern.compile("(?<k>Amazon.ca order number): (?<v>\\d+-\\d+-\\d+)"),
            Pattern.compile("(?<k>Order Placed): (?<v>[A-Z][a-z]+ \\d+, \\d{4})"),
            Pattern.compile("(?<k>Total before tax):\\s?CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Shipping & Handling):\\s?CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Estimated GST/HST):\\s?CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Grand Total):\\s?CDN\\$ (?<v>\\d+\\.\\d{2})"),
    };

    static Order parseText(String text) {
        SortedSet<String> lines = new TreeSet<>(Stream.of(text.split("\r\n"))
                .filter(line -> !line.isBlank())
                .sorted()
                .collect(Collectors.toList()));
        Multimap<String, String> multimap = LinkedListMultimap.create();
        lines.forEach(line -> Stream.of(PATTERNS)
                .map(pattern -> pattern.matcher(line))
                .filter(Matcher::matches)
                .forEach(matcher -> {
                    String key = matcher.group("k");
                    String value = matcher.group("v");
                    multimap.put(key, value);
                }));
        logger.debug("multimap: {}", multimap);
        String id = ListUtils.getSingle(multimap.get("Amazon.ca order number"));
        LocalDate date = LocalDate.parse(ListUtils.getSingle(multimap.get("Order Placed")), DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));
        BigDecimal totalBeforeTax = multimap.get("Total before tax").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal shippingAndHandling = multimap.get("Shipping & Handling").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal hst = multimap.get("Estimated GST/HST").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal total = multimap.get("Grand Total").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        return new Order(id, date, totalBeforeTax, shippingAndHandling, hst, total);
    }

}
