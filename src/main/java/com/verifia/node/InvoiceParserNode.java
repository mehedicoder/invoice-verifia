package com.verifia.node;

import com.verifia.reader.PdfContentReader;
import com.verifia.state.InvoiceState;
import org.bsc.langgraph4j.action.NodeAction;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class InvoiceParserNode implements NodeAction<InvoiceState> {
    @Override
    public Map<String, Object> apply(InvoiceState state) {
        // We now pull the path from the state (passed during invoke)
        String path = (String) state.value("file_path").orElseThrow();
        try {
            List<String> lines = PdfContentReader.read(Paths.get(path));
            return Map.of(InvoiceState.INVOICE_DATA_KEY, lines);
        } catch (Exception e) {
            return Map.of(InvoiceState.MESSAGES_KEY, "Error: " + e.getMessage());
        }
    }
}