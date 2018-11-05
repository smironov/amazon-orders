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
        logger.trace("parsing file: {}", file);
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
            Pattern.compile("(?<k>Item\\(s\\) Subtotal):\\s?CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Total before tax):\\s?CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Shipping & Handling):\\s?CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Environmental Handling Fee) \\s?CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Free Shipping):\\s?-CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Estimated GST/HST):\\s?CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Gift Card Amount):\\s?-CDN\\$ (?<v>\\d+\\.\\d{2})"),
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
        logger.trace("multimap: {}", multimap);
        String id = ListUtils.requireSingle(multimap.get("Amazon.ca order number"));
        LocalDate date = LocalDate.parse(ListUtils.requireSingle(multimap.get("Order Placed")), DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));
        BigDecimal itemsSubtotal = multimap.get("Item(s) Subtotal").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal shippingAndHandling = multimap.get("Shipping & Handling").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal environmentalHandlingFee = ListUtils.getSingle(multimap.get("Environmental Handling Fee").stream().map(BigDecimal::new).collect(Collectors.toList())).orElse(new BigDecimal("0.00"));
        Optional<BigDecimal> freeShipping = ListUtils.getSingle(multimap.get("Free Shipping").stream().map(BigDecimal::new)
                .collect(Collectors.toList()));
        if (freeShipping.isPresent() && (shippingAndHandling.compareTo(freeShipping.get()) == 0)) {
            shippingAndHandling = new BigDecimal("0.00");
        }
        BigDecimal totalBeforeTax = multimap.get("Total before tax").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal hst = multimap.get("Estimated GST/HST").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal total = multimap.get("Grand Total").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        Optional<BigDecimal> giftCardAmount = ListUtils.getSingle(ListUtils.filterDuplicates(multimap.get("Gift Card Amount")).stream().map(BigDecimal::new).collect(Collectors.toList()));
        if (giftCardAmount.isPresent()) {
            total = total.add(giftCardAmount.get());
        }
        String items = lines.stream()
                .filter(line -> line.contains(" of: "))
                .map(line -> line.startsWith("1 of: ") ? line.substring("1 of: ".length()) : line)
                .collect(Collectors.joining("; "));
        try {
            return new Order(id, date, itemsSubtotal, shippingAndHandling, environmentalHandlingFee, totalBeforeTax, hst, total, items);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
