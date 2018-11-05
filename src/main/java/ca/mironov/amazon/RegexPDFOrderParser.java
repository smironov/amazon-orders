package ca.mironov.amazon;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.*;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.regex.*;

public class RegexPDFOrderParser implements OrderParser {

    private static final Logger logger = LoggerFactory.getLogger(RegexPDFOrderParser.class);

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

    private static final Pattern INVOICE_SINGLE_PATTERN = Pattern.compile(
            ".+" +
                    "Item\\(s\\) Subtotal: CDN\\$ (?:\\d+\\.\\d{2})_" +
                    "Shipping & Handling: CDN\\$ (?<sh>\\d+\\.\\d{2})_" +
                    "(?:Free Shipping: -CDN\\$ (?:\\d+\\.\\d{2})_)?" +
                    "(?:Environmental Handling Fee CDN\\$ (?:\\d+\\.\\d{2})_)?" +
                    " -----_" +
                    "Total before tax: CDN\\$ (?<tbt>\\d+\\.\\d{2})_" +
                    "Estimated GST/HST: CDN\\$ (?<hst>\\d+\\.\\d{2})_" +
                    "(?:Estimated PST/RST/QST: CDN\\$ 0.00_)?" +
                    "(?:Gift Card Amount:-CDN\\$ (?:\\d+\\.\\d{2})_)?" +
                    " -----_" +
                    "Grand Total:\\s?CDN\\$ (?<total>\\d+\\.\\d{2})_" +
                    " _" +
                    " _" +
                    "Final Details for Order #(?<id>\\d+-\\d+-\\d+) _" +
                    ".+");

    private static final Pattern INVOICE_MULTIPLE_PATTERN = Pattern.compile(
            ".+" +
                    "Item\\(s\\) Subtotal: CDN\\$ (?:\\d+\\.\\d{2})_" +
                    "Total before tax: CDN\\$ (?<tbt>\\d+\\.\\d{2})_" +
                    "Estimated GST/HST: CDN\\$ (?<hst>\\d+\\.\\d{2})_" +
                    "(?:Estimated PST/RST/QST: CDN\\$ 0.00_)?" +
                    "(?:Gift Card Amount:-CDN\\$ (?:\\d+\\.\\d{2})_)?" +
                    " _" +
                    " _" +
                    "Final Details for Order #(?<id>\\d+-\\d+-\\d+) _" +
                    "Print this page for your records._" +
                    " _" +
                    "Order Placed: (?<date>[A-Za-z] \\d+, \\d{4})_" +
                    ".+" +
                    "Total for this Shipment:CDN\\$ (?<sh>\\d+\\.\\d{2})_" +
                    ".+" +
                    "Grand Total:\\s?CDN\\$ (?<total>\\d+\\.\\d{2})_" +
                    ".+");

    static Order parseText(String text) {
        text = text.replaceAll("\r", "").replaceAll("\n", "_");
        Matcher matcher = INVOICE_SINGLE_PATTERN.matcher(text);
        if (matcher.matches()) {
            String id = matcher.group("id");
            BigDecimal totalBeforeTax = new BigDecimal(matcher.group("tbt"));
            BigDecimal shippingAndHandling = new BigDecimal(matcher.group("sh"));
            BigDecimal hst = new BigDecimal(matcher.group("hst"));
            BigDecimal total = new BigDecimal(matcher.group("total"));
            return new Order(id, null /* todo */, totalBeforeTax, shippingAndHandling, hst, total);
        } else {
            Matcher multipleMatcher = INVOICE_MULTIPLE_PATTERN.matcher(text);
            if (multipleMatcher.matches()) {
                throw new UnsupportedOperationException("not implemented yet"); // todo
            } else {
                throw new IllegalArgumentException(String.format(
                        "invalid invoice:\n%s\n->\n%s", INVOICE_MULTIPLE_PATTERN.pattern(), text));
            }
        }
    }

}
