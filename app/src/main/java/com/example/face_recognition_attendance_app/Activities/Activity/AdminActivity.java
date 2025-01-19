package com.example.face_recognition_attendance_app.Activities.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.face_recognition_attendance_app.Activities.Models.AttendanceDBModel;
import com.example.face_recognition_attendance_app.R;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    MaterialButton downloadBtn;
    RadioGroup radioGroup;
    List<AttendanceDBModel> list = new ArrayList<>();
    private static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        radioGroup = findViewById(R.id.extensionRadioGroup);
        downloadBtn = findViewById(R.id.downloadBtn);


    }
    private void onPermissionGranted(){

        downloadBtn.setOnClickListener(view -> {

            if (radioGroup.getCheckedRadioButtonId() == -1) {
                // No radio button is selected
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
            } else {
                // A radio button is selected
                int selectedId = radioGroup.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = findViewById(selectedId);
                String selectedText = selectedRadioButton.getText().toString();



                if (selectedText.equals("word file")){
                    String filePath = createScopedFilePath("AttendanceRecords", ".word");
                    DownloadExtension.exportToWord(list,filePath);
                }else if (selectedText.equals("pdf file")){
                    String filePath = createScopedFilePath("AttendanceRecords", ".pdf");
                    DownloadExtension.exportToPDF(list,filePath);
                }
                else if (selectedText.equals("excel file")){
                    String filePath = createScopedFilePath("AttendanceRecords", ".excel");
                    DownloadExtension.exportToExcel(list,filePath);
                }
            }


        });
    }
    public String createScopedFilePath(String fileName, String fileExtension) {
        File directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return new File(directory, fileName + fileExtension).getAbsolutePath();
    }

    public void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        } else {
            // Permissions already granted
            onPermissionGranted();
        }
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


}