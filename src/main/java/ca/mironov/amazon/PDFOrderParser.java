package ca.mironov.amazon;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.regex.*;
import java.util.stream.*;

public class PDFOrderParser implements OrderParser {

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

    @SuppressWarnings("InconsistentLineSeparators")
    private static final Pattern[] INVOICE_TEXT_PATTERNS = Stream.of(
            "",
            "Item\\(s\\) Subtotal: CDN\\$ (\\d+\\.\\d{2})",
            "Shipping & Handling: CDN\\$ (\\d+\\.\\d{2})",
            " -----",
            "Total before tax: CDN\\$ (\\d+\\.\\d{2})",
            "Estimated GST/HST: CDN\\$ (\\d+\\.\\d{2})",
            "Estimated PST/RST/QST: CDN\\$ 0.00",
            " -----",
            "Grand Total:CDN\\$ (\\d+\\.\\d{2})",
            "",
            "",
            "Final Details for Order #(\\d+-\\d+-\\d+)"
    ).map(Pattern::compile).collect(Collectors.toList()).toArray(Pattern[]::new);

    protected static Order parseText(String text) {
        String[] lines = text.split("\r\n");
        String[] values = new String[INVOICE_TEXT_PATTERNS.length];
        for (int i = 0; i < INVOICE_TEXT_PATTERNS.length; i++) {
            Pattern pattern = INVOICE_TEXT_PATTERNS[i];
            String line = lines[i].stripTrailing();
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                values[i] = (matcher.groupCount() > 0) ? matcher.group(1) : null;
            } else {
                throw new IllegalArgumentException(
                        "invalid invoice line #" + (1 + i) + ": '" + line + "', expected '" + pattern + "'");
            }
        }
        String id = values[11];
        BigDecimal totalBeforeTax = new BigDecimal(values[4]);
        BigDecimal shippingAndHandling = new BigDecimal(values[2]);
        BigDecimal hst = new BigDecimal(values[5]);
        BigDecimal total = new BigDecimal(values[8]);
        return new Order(id, null /* todo */, totalBeforeTax, shippingAndHandling, hst, total);
    }

}
