package com.verifia.node;

import com.verifia.state.InvoiceState;
import org.bsc.langgraph4j.action.NodeAction;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class PersistenceNode implements NodeAction<InvoiceState> {
    private final String logFile = "verified_invoices.txt";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Map<String, Object> apply(InvoiceState state) {
        // Retrieve state values using helper methods
        String status = state.status();
        String amount = state.totalAmount();
        String approver = state.approver();

        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter out = new PrintWriter(fw)) {

            // Structured log entry
            String logEntry = String.format("[%s] STATUS: %-10s | TOTAL: %-8s | BY: %s",
                    LocalDateTime.now().format(formatter),
                    status.toUpperCase(),
                    amount,
                    approver);

            out.println(logEntry);

            // Enhanced Console Feedback
            if ("Human".equalsIgnoreCase(approver)) {
                // Bright yellow alert for manual actions
                System.out.println("\u001B[33m [Persistence] MANUAL APPROVAL LOGGED: Invoice for $" + amount + " authorized by Human.\u001B[0m");
            } else {
                System.out.println(" [Persistence] Logged " + status + " by " + approver);
            }

            Map<String, Object> result = new HashMap<>();
            result.put(InvoiceState.MESSAGES_KEY, "System: Logged status to " + logFile);
            return result;

        } catch (IOException e) {
            System.err.println("[Persistence] Error writing to log: " + e.getMessage());
        }

        return Map.of();
    }
}