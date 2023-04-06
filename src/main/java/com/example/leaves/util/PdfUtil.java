package com.example.leaves.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class PdfUtil {
    private static final String OUTPUTFILE = "C:/Users/Vladimir/Desktop/ReadPdf.pdf";
    private static final Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
            Font.BOLD);
    private static final Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.NORMAL, BaseColor.RED);
    private static final Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16,
            Font.BOLD);
    private static final Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.BOLD);
    private static String FILE = "C:/Users/Vladimir/Desktop/отпуск.pdf";


    public static File getPdfFile(String fullName, String egn, String Location,
                                  String position, String requestTo, String daysLeave,
                                  String year, String dateFrom, String dateTo) throws IOException, DocumentException {

        //Create PdfReader instance.
        PdfReader pdfReader =
                new PdfReader(FILE);
        File tempFile = File.createTempFile("Tempfile", ".pdf");
        //Create PdfStamper instance.
        PdfStamper pdfStamper = new PdfStamper(pdfReader,
                Files.newOutputStream(Paths.get(OUTPUTFILE)));

        //Create BaseFont instance.
        BaseFont baseFont = BaseFont.createFont(
                BaseFont.TIMES_ROMAN,
                BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

        //Get the number of pages in pdf.
        int pages = pdfReader.getNumberOfPages();

        //write fullName on pdf
        writeTextOnPdf(pdfStamper, baseFont, 1, "Gosho", 110, 644);

        //write egn on pdf
        writeTextOnPdf(pdfStamper, baseFont, 1, "egn", 85, 610);

        //write Location on pdf
        writeTextOnPdf(pdfStamper, baseFont, 1, "Location", 295, 610);

        //write position on pdf
        writeTextOnPdf(pdfStamper, baseFont, 1, "position", 130, 575);

        //write requestTo on pdf
        writeTextOnPdf(pdfStamper, baseFont, 1, "requestTo", 210, 515);

        //write daysLeave on pdf
        writeTextOnPdf(pdfStamper, baseFont, 1, "daysLeave", 260, 485);

        //write year on pdf
        writeTextOnPdf(pdfStamper, baseFont, 1, "year", 80, 473);

        //write dateFrom on pdf
        writeTextOnPdf(pdfStamper, baseFont, 1, "dateFrom", 201, 473);

        //write dateTo on pdf
        writeTextOnPdf(pdfStamper, baseFont, 1, "dateTo", 450, 473);

        //draw line
        drawLineOnPdf(pdfStamper, baseFont, 1, 450, 473, true);

        //Close the pdfStamper.
        pdfStamper.close();
        return new File(tempFile.getPath());

    }

    private static void writeTextOnPdf(PdfStamper pdfStamper, BaseFont baseFont, int pageNum, String text, float x, float y) {
//Contain the pdf data.
        PdfContentByte pageContentByte =
                pdfStamper.getOverContent(pageNum);

        pageContentByte.beginText();
//Set text font and size.
        pageContentByte.setFontAndSize(baseFont, 14);

        pageContentByte.setTextMatrix(x, y);

//Write text
        pageContentByte.showText(text);
        pageContentByte.endText();
    }

    private static void drawLineOnPdf(PdfStamper pdfStamper, BaseFont baseFont,
                                      int pageNum, float x,
                                      float y, boolean isPaid) throws DocumentException, IOException {
        if (isPaid) {
            //Contain the pdf data.
            PdfContentByte pageContentByte =
                    pdfStamper.getOverContent(pageNum);

            pageContentByte.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false), 24);
            pageContentByte.moveTo(443, 487);
            pageContentByte.lineTo(390, 487);
            pageContentByte.stroke();

        } else {
            PdfContentByte pageContentByte =
                    pdfStamper.getOverContent(pageNum);

            pageContentByte.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false), 24);
            pageContentByte.moveTo(380, 487);
            pageContentByte.lineTo(338, 487);
            pageContentByte.stroke();
        }

    }
}