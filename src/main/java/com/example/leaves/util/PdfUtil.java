package com.example.leaves.util;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfUtil {
    private static final String OUTPUTFILE = "C:/Users/Vladimir/Desktop/ReadPdfssssddddddss.pdf";

    private static String FILE = "src/main/resources/docx/отпуск.docx";

    public static byte[] replaceWords(Map<String, String> words) throws IOException, InvalidFormatException {
        try {

            /**
             * if uploaded doc then use HWPF else if uploaded Docx file use
             * XWPFDocument
             */
            File tempFile = File.createTempFile("Request", ".pdf");

            XWPFDocument doc = new XWPFDocument(
                    OPCPackage.open(FILE));
            for (XWPFParagraph p : doc.getParagraphs()) {
                List<XWPFRun> runs = p.getRuns();
                if (runs != null) {
                    for (XWPFRun r : runs) {
                        replaceWords(words, r);
                    }
                }
            }

            for (XWPFTable tbl : doc.getTables()) {
                for (XWPFTableRow row : tbl.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            for (XWPFRun r : p.getRuns()) {
                                replaceWords(words, r);
                            }
                        }
                    }
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfConverter pdfConverter = new PdfConverter();
//            pdfConverter.convert(doc, baos, PdfOptions.getDefault());

            return baos.toByteArray();
        } finally {

        }
    }

    private static void replaceWords(Map<String, String> words, XWPFRun r) {
        String text = r.getText(0);
        for (String key : words.keySet()
        ) {
            if (text != null && text.contains(key)) {
                text = text.replace(key, words.get(key));
                r.setText(text, 0);
            }
        }
    }

}
