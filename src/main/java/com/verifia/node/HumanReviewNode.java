package com.verifia.node;

import com.verifia.state.InvoiceState;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

// This node simply marks that we are ready for review
class HumanReviewNode implements NodeAction<InvoiceState> {
    @Override
    public Map<String, Object> apply(InvoiceState state) {
        System.out.println("--- System: Document verification prepared. Waiting for Human Review ---");
        return Map.of(); // The graph will pause here via configuration
    }
}