package ca.mironov.amazon;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.regex.*;

public class RegexPDFOrderParser implements OrderParser {

    @Override
    public Order parse(Path file) {
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

    @SuppressWarnings({"InconsistentLineSeparators", "HardcodedLineSeparator", "SingleCharacterStringConcatenation"})
    private static final Pattern INVOICE_TEXT_PATTERN = Pattern.compile(
            " _" +
                    "Item\\(s\\) Subtotal: CDN\\$ (?:\\d+\\.\\d{2})_" +
                    "Shipping & Handling: CDN\\$ (?<sh>\\d+\\.\\d{2})_" +
                    " -----_" +
                    "Total before tax: CDN\\$ (?<tbt>\\d+\\.\\d{2})_" +
                    "Estimated GST/HST: CDN\\$ (?<hst>\\d+\\.\\d{2})_" +
                    "Estimated PST/RST/QST: CDN\\$ 0.00_" +
                    " -----_" +
                    "Grand Total:CDN\\$ (?<total>\\d+\\.\\d{2})_" +
                    " _" +
                    " _" +
                    "Final Details for Order #(?<id>\\d+-\\d+-\\d+) _" +
                    ".+");

    static Order parseText(String text) {
        text = text.replaceAll("\r", "").replaceAll("\n", "_");
        Matcher matcher = INVOICE_TEXT_PATTERN.matcher(text);
        if (matcher.matches()) {
            String id = matcher.group("id");
            BigDecimal totalBeforeTax = new BigDecimal(matcher.group("tbt"));
            BigDecimal shippingAndHandling = new BigDecimal(matcher.group("sh"));
            BigDecimal hst = new BigDecimal(matcher.group("hst"));
            BigDecimal total = new BigDecimal(matcher.group("total"));
            return new Order(id, totalBeforeTax, shippingAndHandling, hst, total);
        } else {
            throw new IllegalArgumentException(String.format("invalid invoice:\n%s\n%s", text, INVOICE_TEXT_PATTERN.pattern()));
        }
    }

}
