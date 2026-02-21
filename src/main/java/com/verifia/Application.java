package com.verifia;

import com.verifia.node.*;
import com.verifia.state.InvoiceState;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.Channel;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.StateGraph.END;

public class Application {

    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BOLD = "\u001B[1m";

    public static void main(String[] args) throws Exception {
        String outputFolder = "C:/projects/invoice-verifia/invoices_queue/";
        Scanner scanner = new Scanner(System.in);

        File dir = new File(outputFolder);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) file.delete();
            }
            System.out.println(YELLOW + "🧹 Workspace cleared." + RESET);
        } else {
            dir.mkdirs();
        }

        // 2. Define the Graph
        Map<String, Channel<?>> schema = Map.of();
        StateGraph<InvoiceState> workflow = new StateGraph<>(schema, InvoiceState::new);

        workflow.addNode("parser", node_async(new InvoiceParserNode()));
        workflow.addNode("extractor", node_async(new AmountExtractorNode()));

        workflow.addNode("human_review", node_async((InvoiceState state) -> Collections.<String, Object>emptyMap()));

        workflow.addNode("auto_approve_logic", node_async((InvoiceState state) -> Map.of(
                InvoiceState.STATUS_KEY, "APPROVED",
                InvoiceState.APPROVER_KEY, "Agent"
        )));

        workflow.addNode("persistence", node_async(new PersistenceNode()));
        workflow.addNode("responder", node_async(new ResponderNode()));

        // 3. Define Edges & Routing
        workflow.addEdge(START, "parser");
        workflow.addEdge("parser", "extractor");

        workflow.addConditionalEdges("extractor", state -> {
            try {
                double amount = Double.parseDouble(state.totalAmount().trim());
                return CompletableFuture.completedFuture((amount > 0 && amount < 100.0) ? "auto" : "manual");
            } catch (Exception e) {
                return CompletableFuture.completedFuture("manual");
            }
        }, Map.of("auto", "auto_approve_logic", "manual", "human_review"));

        workflow.addEdge("auto_approve_logic", "persistence");
        workflow.addEdge("human_review", "persistence");

        workflow.addConditionalEdges("persistence", state -> {
            if ("APPROVED".equalsIgnoreCase(state.status())) {
                return CompletableFuture.completedFuture("finish");
            } else {
                return CompletableFuture.completedFuture("exit"); // REJECTED or ERROR exits
            }
        }, Map.of("finish", "responder", "exit", END));

        workflow.addEdge("responder", END);

        var graph = workflow.compile(CompileConfig.builder()
                .checkpointSaver(new MemorySaver())
                .interruptBefore("human_review")
                .build());

        // 4. Processing Loop
        System.out.println(BOLD + "\nSTARTING STREAMING SESSION" + RESET);

        for (int i = 1; i <= 10; i++) {
            String fileName = "invoice_" + i + ".pdf";
            double testAmount = (i % 3 == 0) ? 150.00 : 45.00;

            InvoiceTestGenerator.generate(outputFolder, fileName, testAmount);
            Thread.sleep(300);

            String fullPath = outputFolder + fileName;
            var runConfig = RunnableConfig.builder().threadId("invoice-" + i).build();

            // First Invoke: Runs until the interrupt at 'human_review'
            graph.invoke(Map.of("file_path", fullPath), runConfig);

            var snapshot = graph.getState(runConfig);
            if (snapshot.next().contains("human_review")) {
                System.out.println(YELLOW + "Total Detected: $" + snapshot.state().totalAmount() + RESET);
                System.out.print("Approve [A] or Reject [R]? ");
                String input = scanner.nextLine();

                String status = "A".equalsIgnoreCase(input) ? "APPROVED" : "REJECTED";

                // Update the state with the Human's decision
                graph.updateState(runConfig, Map.of(
                        InvoiceState.STATUS_KEY, status,
                        InvoiceState.APPROVER_KEY, "Human"
                ));

                // SECOND INVOKE (RESUME): Passing (Map)null tells LangGraph to continue
                graph.invoke((Map<String, Object>) null, runConfig);
            }

            System.out.println(GREEN + "Processed " + fileName + RESET);
        }

        System.out.println("\n" + BOLD + GREEN + "ALL INVOICES PROCESSED." + RESET);
        scanner.close();
    }
}