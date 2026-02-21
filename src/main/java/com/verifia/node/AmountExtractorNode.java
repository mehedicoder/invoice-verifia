package com.verifia.node;

import com.verifia.state.InvoiceState;
import org.bsc.langgraph4j.action.NodeAction;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AmountExtractorNode implements NodeAction<InvoiceState> {
    @Override
    public Map<String, Object> apply(InvoiceState state) {
        // Updated Regex:
        // 1. (?i)(total|amount due|total amount|amount) -> Matches labels case-insensitively
        // 2. [:\s]*[^0-9]* -> Matches colon, spaces, or currency symbols
        // 3. ([\d,.]+) -> Captures the numeric part including commas and dots
        Pattern pattern = Pattern.compile("(?i)(total|amount due|total amount|amount)[:\\s]*[^0-9]*([\\d,.]+)");

        String foundAmount = "0.00";

        for (String line : state.invoiceData()) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String rawAmount = matcher.group(2);

                // Normalization Logic:
                // If it ends with ,XX (like 45,00), convert to .XX
                if (rawAmount.matches(".*,\\d{2}$")) {
                    foundAmount = rawAmount.replace(",", ".");
                } else {
                    // Otherwise just remove thousands-separator commas
                    foundAmount = rawAmount.replace(",", "");
                }
                break;
            }
        }

        System.out.println("\u001B[36m[Extractor] Detected: $" + foundAmount + "\u001B[0m");

        return Map.of(InvoiceState.TOTAL_AMOUNT_KEY, foundAmount);
    }
}