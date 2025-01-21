package com.example.face_recognition_attendance_app.Activities.Activity;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.face_recognition_attendance_app.Activities.Connectivity.HttpWeb;
import com.example.face_recognition_attendance_app.Activities.Models.AttendanceDBModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DownloadExtension {

    public static void exportToPDF(List<AttendanceDBModel> records, String filePath,Context context,FileCreationCallback creationCallback) {
        try {
            File file = new File(filePath);
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Attendance Records").setBold().setFontSize(16));

            // Group records by name
            Map<String, List<AttendanceDBModel>> groupedRecords = new HashMap<>();
            for (AttendanceDBModel record : records) {
                String name = record.getName();
                if (!groupedRecords.containsKey(name)) {
                    groupedRecords.put(name, new ArrayList<>());
                }
                groupedRecords.get(name).add(record);
            }

            // Loop through grouped records and add to PDF
            for (Map.Entry<String, List<AttendanceDBModel>> entry : groupedRecords.entrySet()) {
                String name = entry.getKey();
                List<AttendanceDBModel> groupedList = entry.getValue();

                // Add Name as a section heading
                document.add(new Paragraph("Name: "+name).setBold().setFontSize(14));

                for (AttendanceDBModel record : groupedList) {
                    String data = "Check-In Date: " + record.getCheckInDate() +
                            "\nCheck-In Time: " + record.getCheckInTime() +
                            "\nCheck-Out Time: " + record.getCheckOutTime() +
                            "\nTotal Time: " + calculateTime(record.getCheckInTime(), record.getCheckOutTime()) +
                            "\n\n"; // Space between records
                    document.add(new Paragraph(data));
                }
            }

            document.close();
            Toast.makeText(context, "File Saved Successfully", Toast.LENGTH_SHORT).show();
            System.out.println("PDF created at " + filePath);
            if (creationCallback != null){
                creationCallback.onFileCreated();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void exportToWord(List<AttendanceDBModel> records, String filePath, Context context,FileCreationCallback creationCallback) {
        try {
            XWPFDocument document = new XWPFDocument();
            XWPFParagraph title = document.createParagraph();
            XWPFRun titleRun = title.createRun();
            titleRun.setText("Attendance Records");
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            // Group records by name
            Map<String, List<AttendanceDBModel>> groupedRecords = new HashMap<>();
            for (AttendanceDBModel record : records) {
                String name = record.getName();
                if (!groupedRecords.containsKey(name)) {
                    groupedRecords.put(name, new ArrayList<>());
                }
                groupedRecords.get(name).add(record);
            }

            // Loop through grouped records and add to Word document
            for (Map.Entry<String, List<AttendanceDBModel>> entry : groupedRecords.entrySet()) {
                String name = entry.getKey();
                List<AttendanceDBModel> groupedList = entry.getValue();

                // Add Name as a section heading
                XWPFParagraph nameParagraph = document.createParagraph();
                XWPFRun nameRun = nameParagraph.createRun();
                nameRun.setText("Name: "+name);
                nameRun.setBold(true);
                nameRun.setFontSize(14);

                // Add Attendance records for each name
                for (AttendanceDBModel record : groupedList) {
                    String data = "Check-In Date: " + record.getCheckInDate() +
                            "\nCheck-In Time: " + record.getCheckInTime() +
                            "\nCheck-Out Time: " + record.getCheckOutTime() +
                            "\nTotal Time: " + calculateTime(record.getCheckInTime(), record.getCheckOutTime()) +
                            "\n\n"; // Space between records
                    XWPFParagraph paragraph = document.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    run.setText(data);
                }
            }

            // Write to file
            File file = new File(filePath);
            FileOutputStream out = new FileOutputStream(file);
            document.write(out);
            out.close();
            document.close();
            Toast.makeText(context, "File Saved Successfully", Toast.LENGTH_SHORT).show();
            System.out.println("Word file created at " + filePath);
            if (creationCallback != null){
                creationCallback.onFileCreated();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportToExcel(List<AttendanceDBModel> records, String filePath, Context context,FileCreationCallback creationCallback) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Attendance Records");

            // Create header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Check-In Date");
            header.createCell(2).setCellValue("Check-In Time");
            header.createCell(3).setCellValue("Check-Out Time");
            header.createCell(4).setCellValue("Total Time");

            // Group records by name
            Map<String, List<AttendanceDBModel>> groupedRecords = new HashMap<>();
            for (AttendanceDBModel record : records) {
                String name = record.getName();
                if (!groupedRecords.containsKey(name)) {
                    groupedRecords.put(name, new ArrayList<>());
                }
                groupedRecords.get(name).add(record);
            }

            // Fill data with name as a group and attendance records underneath
            int rowIndex = 1;
            for (Map.Entry<String, List<AttendanceDBModel>> entry : groupedRecords.entrySet()) {
                String name = entry.getKey();
                List<AttendanceDBModel> groupedList = entry.getValue();

                // Add name in a new row
                Row nameRow = sheet.createRow(rowIndex++);
                nameRow.createCell(0).setCellValue(name);

                // Add attendance records for each name
                for (AttendanceDBModel record : groupedList) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(1).setCellValue(record.getCheckInDate());
                    row.createCell(2).setCellValue(record.getCheckInTime());
                    row.createCell(3).setCellValue(record.getCheckOutTime());
                    row.createCell(4).setCellValue(calculateTime(record.getCheckInTime(),record.getCheckOutTime()));
                }
            }

            // Write to file
            File file = new File(filePath);
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            Toast.makeText(context, "File Saved Successfully", Toast.LENGTH_SHORT).show();
            System.out.println("Excel file created at " + filePath);
            if (creationCallback != null){
                creationCallback.onFileCreated();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String  calculateTime(String checkInTime,String checkOutTime){

        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss:aa");
        String formattedTime = "";
        try {
            // Parse the time strings into Date objects
            Date checkInDate = format.parse(checkInTime);
            Date checkOutDate = format.parse(checkOutTime);

            // Calculate the difference in milliseconds
            long differenceInMillis = checkOutDate.getTime() - checkInDate.getTime();

            // Convert the difference to seconds (or other units)
            long differenceInSeconds = TimeUnit.MILLISECONDS.toSeconds(differenceInMillis);
            long hours = differenceInSeconds / 3600;
            long minutes = (differenceInSeconds % 3600) / 60;
            formattedTime = String.format("%02dH %02dM", hours, minutes);

            System.out.println("Time Difference: " + formattedTime + " seconds");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedTime;
    }
    public interface FileCreationCallback {
        void onFileCreated();
    }

}
