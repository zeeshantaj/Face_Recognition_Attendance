package com.example.face_recognition_attendance_app.Activities.Activity;

import android.content.Context;
import android.widget.Toast;

import com.example.face_recognition_attendance_app.Activities.Models.AttendanceDBModel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class DownloadExtension {

    public static void exportToPDF(List<AttendanceDBModel> records, String filePath,Context context) {
        try {
            File file = new File(filePath);
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Attendance Records").setBold().setFontSize(16));
            for (AttendanceDBModel record : records) {
                String data = "ID: " + record.getId() +
                        "\nName: " + record.getName() +
                        "\nCheck-In Time: " + record.getCheckInTime() +
                        "\nCheck-In Date: " + record.getCheckInDate() +
                        "\nCheck-Out Time: " + record.getCheckOutTime() +
                        "\n\n";
                document.add(new Paragraph(data));
            }

            document.close();
            Toast.makeText(context, "File Saved Successfully", Toast.LENGTH_SHORT).show();
            System.out.println("PDF created at " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void exportToWord(List<AttendanceDBModel> records, String filePath, Context context) {
        try {
            XWPFDocument document = new XWPFDocument();
            XWPFParagraph title = document.createParagraph();
            XWPFRun titleRun = title.createRun();
            titleRun.setText("Attendance Records");
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            for (AttendanceDBModel record : records) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                String data = "ID: " + record.getId() +
                        "\nName: " + record.getName() +
                        "\nCheck-In Time: " + record.getCheckInTime() +
                        "\nCheck-In Date: " + record.getCheckInDate() +
                        "\nCheck-Out Time: " + record.getCheckOutTime() +
                        "\n";
                run.setText(data);
            }

            // Write to file
            File file = new File(filePath);
            FileOutputStream out = new FileOutputStream(file);
            document.write(out);
            out.close();
            document.close();
            Toast.makeText(context, "File Saved Successfully", Toast.LENGTH_SHORT).show();
            System.out.println("Word file created at " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void exportToExcel(List<AttendanceDBModel> records, String filePath,Context context) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Attendance Records");

            // Create header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Check-In Time");
            header.createCell(3).setCellValue("Check-In Date");
            header.createCell(4).setCellValue("Check-Out Time");

            // Fill data
            int rowIndex = 1;
            for (AttendanceDBModel record : records) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(record.getId());
                row.createCell(1).setCellValue(record.getName());
                row.createCell(2).setCellValue(record.getCheckInTime());
                row.createCell(3).setCellValue(record.getCheckInDate());
                row.createCell(4).setCellValue(record.getCheckOutTime());
            }

            // Write to file
            File file = new File(filePath);
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            Toast.makeText(context, "File Saved Successfully", Toast.LENGTH_SHORT).show();
            System.out.println("Excel file created at " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
