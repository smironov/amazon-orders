package ca.mironov.amazon;

import ca.mironov.amazon.util.Multimap;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class KeyPDFOrderParser implements OrderParser {

    private static final Logger logger = LoggerFactory.getLogger(KeyPDFOrderParser.class);

    @Override
    public Order parse(Path file) throws IOException {
        logger.debug("parsing file: {}", file);
        try (InputStream in = Files.newInputStream(file)) {
            //noinspection NestedTryStatement
            try (PDDocument document = PDDocument.load(in)) {
                if (document.isEncrypted())
                    throw new IllegalArgumentException("document is encrypted: " + file);
                String text = new PDFTextStripper().getText(document);
                return parseText(text);
            }
        }
    }

    private static final Pattern[] PATTERNS = {
            Pattern.compile("(?<k>Amazon.ca order number): (?<v>\\d+-\\d+-\\d+)"),
            Pattern.compile("(?<k>Order Placed): (?<v>[A-Z][a-z]+ \\d+, \\d{4})"),
            Pattern.compile("(?<k>Item\\(s\\) Subtotal):\\s?\\$(?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Total before tax):\\s?\\$(?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Shipping & Handling):\\s?\\$(?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Your Coupon Savings):\\s?-\\$(?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Lightning Deal):\\s?-\\$(?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Environmental Handling Fee) \\s?\\$(?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Promotion Applied):\\s?-\\$(?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Free Shipping):\\s?-\\$(?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>FREE Shipping):\\s?-\\$(?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Import Fees Deposit):\\s?\\$(?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Estimated GST/HST):\\s?\\$(?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Gift Card Amount):\\s?-\\$(?<v>\\d+\\.\\d{2})"),
            Pattern.compile("(?<k>Grand Total):\\s?\\$(?<v>\\d+\\.\\d{2})(?:Canada)?"), // Canada for workaround
    };

    private static Order parseText(String text) {
        //noinspection DynamicRegexReplaceableByCompiledPattern,HardcodedLineSeparator
        SortedSet<String> lines = Stream.of(text.split("\r\n"))
                .filter(line -> !line.isBlank())
                .sorted().collect(Collectors.toCollection(TreeSet::new));
        Multimap<String, String> multimap = new Multimap<>();
        lines.stream().filter(line -> line.contains("Grand")).forEach(logger::trace);
        lines.forEach(line -> Stream.of(PATTERNS)
                .map(pattern -> pattern.matcher(line))
                .filter(Matcher::matches)
                .forEach(matcher ->
                        multimap.put(matcher.group("k"), matcher.group("v"))));
        logger.trace("multimap: {}", multimap);
        String id = multimap.getOnlyElement("Amazon.ca order number");
        LocalDate date = LocalDate.parse(multimap.getOnlyElement("Order Placed"), DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));
        BigDecimal itemsSubtotal = multimap.get("Item(s) Subtotal").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalArgumentException("\"Item(s) Subtotal\" not found in: " + multimap.keySet()));
        BigDecimal shippingAndHandling = multimap.get("Shipping & Handling").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal yourCouponSavings = multimap.get("Your Coupon Savings").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElse(Order.FINANCIAL_ZERO);
        BigDecimal lightningDeal = multimap.get("Lightning Deal").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElse(Order.FINANCIAL_ZERO);
        BigDecimal promotionApplied = multimap.findOnlyElement("Promotion Applied").map(BigDecimal::new).orElse(Order.FINANCIAL_ZERO);
        BigDecimal discount = yourCouponSavings.add(lightningDeal).add(promotionApplied);
        BigDecimal environmentalHandlingFee = multimap.findOnlyElement("Environmental Handling Fee").map(BigDecimal::new).orElse(Order.FINANCIAL_ZERO);
        Optional<BigDecimal> freeShipping1 = multimap.findOnlyElement("Free Shipping").map(BigDecimal::new);
        Optional<BigDecimal> freeShipping2 = multimap.findOnlyElement("FREE Shipping").map(BigDecimal::new);
        Optional<BigDecimal> freeShipping = freeShipping1.map(fs1 -> freeShipping2.map(fs1::add).orElse(fs1)).or(() -> freeShipping2);
        if (freeShipping.isPresent() && (shippingAndHandling.compareTo(freeShipping.get()) == 0))
            shippingAndHandling = Order.FINANCIAL_ZERO;
        BigDecimal totalBeforeTax = multimap.get("Total before tax").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal importFeesDeposit = multimap.get("Import Fees Deposit").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElse(Order.FINANCIAL_ZERO);
        BigDecimal hst = multimap.get("Estimated GST/HST").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal total = multimap.get("Grand Total").stream().map(BigDecimal::new)
                .max(Comparator.naturalOrder()).orElseThrow(() -> new IllegalArgumentException("Grand Total not present"));
        Optional<BigDecimal> giftCardAmount = multimap.findOnlyElement("Gift Card Amount").map(BigDecimal::new);
        if (giftCardAmount.isPresent())
            total = total.add(giftCardAmount.get());
        String items = lines.stream()
                .filter(line -> line.contains(" of: "))
                .map(line -> line.startsWith("1 of: ") ? line.substring("1 of: ".length()) : line)
                .collect(Collectors.joining("; "));
        return new Order(
                id, date, itemsSubtotal, shippingAndHandling, discount, environmentalHandlingFee, totalBeforeTax, importFeesDeposit, hst, total, items);
    }

}
