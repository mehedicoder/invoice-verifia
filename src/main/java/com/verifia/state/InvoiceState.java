package com.verifia.state;

import org.bsc.langgraph4j.state.AgentState;
import java.util.Map;
import java.util.Optional;
import java.util.List;

public class InvoiceState extends AgentState {
    public static final String INVOICE_DATA_KEY = "invoice_lines";
    public static final String TOTAL_AMOUNT_KEY = "total_amount";
    public static final String STATUS_KEY = "status";
    public static final String APPROVER_KEY = "approver";
    public static final String MESSAGES_KEY = "messages";

    public InvoiceState(Map<String, Object> initData) {
        super(initData);
    }

    public <T> Optional<T> getVal(String key) {
        return Optional.ofNullable((T) data().get(key));
    }

    @SuppressWarnings("unchecked")
    public List<String> invoiceData() {
        return (List<String>) data().getOrDefault(INVOICE_DATA_KEY, List.of());
    }

    public String totalAmount() {
        Object amount = data().get(TOTAL_AMOUNT_KEY);
        return amount != null ? amount.toString() : "0.00";
    }

    public String status() {
        Object status = data().get(STATUS_KEY);
        return status != null ? status.toString() : "PENDING";
    }

    // Fixed: Added the missing approver() method
    public String approver() {
        Object approver = data().get(APPROVER_KEY);
        return approver != null ? approver.toString() : "Unknown";
    }
}