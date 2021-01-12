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

import static ca.mironov.amazon.util.ListUtils.findOnlyElement;

public class KeyPDFOrderParser implements OrderParser {

    private static final Logger logger = LoggerFactory.getLogger(KeyPDFOrderParser.class);

    @Override
    public Order parse(Path file) throws IOException {
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
        }
    }

    private static final Pattern[] PATTERNS = {
            Pattern.compile("(?<k>Amazon.ca order number): (?<v>\\d+-\\d+-\\d+)"),
            Pattern.compile("(?<k>Order Placed): (?<v>[A-Z][a-z]+ \\d+, \\d{4})"),
            Pattern.compile("(?<k>Item\\(s\\) Subtotal):\\s?CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Total before tax):\\s?CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Shipping & Handling):\\s?CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Your Coupon Savings):\\s?-CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Lightning Deal):\\s?-CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Environmental Handling Fee) \\s?CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Promotion Applied):\\s?-CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Free Shipping):\\s?-CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>FREE Shipping):\\s?-CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Estimated GST/HST):\\s?CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Gift Card Amount):\\s?-CDN\\$ (?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Grand Total):\\s?CDN\\$ (?<v>\\d+\\.\\d{2})(?:Canada)?"), // Canada for workaround
    };

    private static Order parseText(String text) {
        //noinspection DynamicRegexReplaceableByCompiledPattern,HardcodedLineSeparator
        SortedSet<String> lines = Stream.of(text.split("\r\n"))
                .filter(line -> !line.isBlank())
                .sorted().collect(Collectors.toCollection(TreeSet::new));
        Multimap<String, String> multimap = LinkedListMultimap.create();
        lines.stream().filter(line -> line.contains("Grand")).forEach(logger::trace);
        lines.forEach(line -> Stream.of(PATTERNS)
                .map(pattern -> pattern.matcher(line))
                .filter(Matcher::matches)
                .forEach(matcher -> {
                    String key = matcher.group("k");
                    String value = matcher.group("v");
                    multimap.put(key, value);
                }));
        logger.trace("multimap: {}", multimap);
        String id = Iterables.getOnlyElement(multimap.get("Amazon.ca order number"));
        LocalDate date = LocalDate.parse(Iterables.getOnlyElement(multimap.get("Order Placed")), DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));
        BigDecimal itemsSubtotal = multimap.get("Item(s) Subtotal").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal shippingAndHandling = multimap.get("Shipping & Handling").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal yourCouponSavings = multimap.get("Your Coupon Savings").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElse(Order.FINANCIAL_ZERO);
        BigDecimal lightningDeal = multimap.get("Lightning Deal").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElse(Order.FINANCIAL_ZERO);
        BigDecimal promotionApplied = findOnlyElement(multimap.get("Promotion Applied")).map(BigDecimal::new).orElse(Order.FINANCIAL_ZERO);
        BigDecimal discount = yourCouponSavings.add(lightningDeal).add(promotionApplied);
        BigDecimal environmentalHandlingFee = findOnlyElement(multimap.get("Environmental Handling Fee")).map(BigDecimal::new).orElse(Order.FINANCIAL_ZERO);
        Optional<BigDecimal> freeShipping1 = findOnlyElement(multimap.get("Free Shipping")).map(BigDecimal::new);
        Optional<BigDecimal> freeShipping2 = findOnlyElement(multimap.get("FREE Shipping")).map(BigDecimal::new);
        Optional<BigDecimal> freeShipping = freeShipping1.map(fs1 -> freeShipping2.map(fs1::add).orElse(fs1)).or(() -> freeShipping2);
        if (freeShipping.isPresent() && (shippingAndHandling.compareTo(freeShipping.get()) == 0)) {
            shippingAndHandling = Order.FINANCIAL_ZERO;
        }
        BigDecimal totalBeforeTax = multimap.get("Total before tax").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal hst = multimap.get("Estimated GST/HST").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal total = multimap.get("Grand Total").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow(() -> new IllegalArgumentException("Grand Total not present"));
        Optional<BigDecimal> giftCardAmount = findOnlyElement(new HashSet<>(multimap.get("Gift Card Amount"))).map(BigDecimal::new);
        if (giftCardAmount.isPresent()) {
            total = total.add(giftCardAmount.get());
        }
        String items = lines.stream()
                .filter(line -> line.contains(" of: "))
                .map(line -> line.startsWith("1 of: ") ? line.substring("1 of: ".length()) : line)
                .collect(Collectors.joining("; "));
        return new Order(id, date, itemsSubtotal, shippingAndHandling, discount, environmentalHandlingFee, totalBeforeTax, hst, total, items);
    }

}
