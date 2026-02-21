package com.verifia;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class InvoiceTestGenerator {

    public static void generate(String folderPath, String fileName, double amount) throws IOException {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, fileName);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("INVOICE: " + fileName);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.setLeading(15);
                contentStream.showText("Vendor: Verifia Test Systems");
                contentStream.newLine();
                contentStream.showText("Date: February 21, 2026");
                contentStream.newLine();
                contentStream.newLine();
                contentStream.showText("Description: Service Subscription");
                contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                // Fix: Explicitly use Locale.US to force a dot decimal separator
                contentStream.showText("Total Amount Due: " + String.format(Locale.US, "%.2f", amount));

                contentStream.endText();
            }
            document.save(file);
        }
    }
}