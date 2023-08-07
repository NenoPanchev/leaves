package com.example.leaves.util;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;

import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


public class PdfUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PdfUtil.class);

    private static final String FILE = "static/docx/отпуск.docx";

    private PdfUtil() {
        throw new IllegalStateException("Util class");
    }

    public static byte[] replaceWords(Map<String, String> words) throws IOException, InvalidFormatException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             InputStream inputStream = PdfUtil.class.getClassLoader().getResourceAsStream(FILE)) {

            /**
             * if uploaded doc then use HWPF else if uploaded Docx file use
             * XWPFDocument
             */

            assert inputStream != null;
            XWPFDocument doc = new XWPFDocument(
                    OPCPackage.open(inputStream));
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
            PdfOptions options = PdfOptions.create();
            options.fontProvider(getFontProvider());

            PdfConverter.getInstance().convert(doc, baos, options);
            return baos.toByteArray();
        }
    }

    private static void replaceWords(Map<String, String> words, XWPFRun r) {
        String text = r.getText(0);
        for (Map.Entry<String, String> entry : words.entrySet()) {
            if (text != null && text.contains(entry.getKey())) {
                text = text.replace(entry.getKey(), entry.getValue());
                r.setText(text, 0);
            }
        }
    }

    private static IFontProvider getFontProvider() {
        return (familyName, encoding, size, style, color) -> {
            try {
                if (familyName.equalsIgnoreCase("Times New Roman")) {
                    BaseFont baseFont;
                    if (style == Font.BOLD) {
                        baseFont = BaseFont.createFont("static/ttf/Times New RomanB.ttf", encoding, BaseFont.EMBEDDED);
                    } else {
                        baseFont = BaseFont.createFont("static/ttf/Times New Roman.ttf", encoding, BaseFont.EMBEDDED);
                    }
                    return new Font(baseFont, size, style, color);
                } else if (familyName.equalsIgnoreCase("Calibri") || encoding.equalsIgnoreCase(BaseFont.IDENTITY_H)) {
                    BaseFont baseFont;
                    if (style == Font.BOLD) {
                        baseFont = BaseFont.createFont("static/ttf/CalibriB.ttf", encoding, BaseFont.EMBEDDED);
                    } else {
                        baseFont = BaseFont.createFont("static/ttf/Calibri.ttf", encoding, BaseFont.EMBEDDED);
                    }
                    return new Font(baseFont, size, style, color);
                }
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            return FontFactory.getFont(familyName, encoding, size, style, color);
        };
    }

}
