package com.verifia.node;

import com.verifia.state.InvoiceState;
import org.bsc.langgraph4j.action.NodeAction;
import java.util.Collections;
import java.util.Map;

public class ResponderNode implements NodeAction<InvoiceState> {
    @Override
    public Map<String, Object> apply(InvoiceState state) {
        String status = state.status();
        String amount = state.totalAmount();

        System.out.println(String.format("📧 [Responder] Finalized Invoice: Status=%s, Total=$%s",
                status,
                amount));

        return Collections.emptyMap();
    }
}