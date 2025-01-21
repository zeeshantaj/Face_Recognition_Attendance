package com.example.face_recognition_attendance_app.Activities.Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.face_recognition_attendance_app.Activities.Util.UiHelper;
import com.example.face_recognition_attendance_app.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mukeshsolanki.OtpView;

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

        checkAndRequestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        list = getAllData();
        listFilesInDirectory();
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
                enterConfigPin(selectedText);
            }
        });
    }

    private void enterConfigPin(String selectedText) {
        final Dialog ipDialog = new Dialog(this);
        ipDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ipDialog.setContentView(R.layout.name_dialog);
        ipDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button continueBtn = ipDialog.findViewById(R.id.enterBtn);
        Button cancelBtn = ipDialog.findViewById(R.id.cancelBtn);
        EditText nameEdt = ipDialog.findViewById(R.id.otp_view);

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ipDialog.dismiss();
                String pin = nameEdt.getText().toString();
                if (!pin.isEmpty()){

                    switch (selectedText) {
                        case "word file": {
                            String filePath = createScopedFilePath(pin, ".word");
                            DownloadExtension.exportToWord(list, filePath, AdminActivity.this);
                            break;
                        }
                        case "pdf file": {
                            String filePath = createScopedFilePath(pin, ".pdf");
                            DownloadExtension.exportToPDF(list, filePath, AdminActivity.this);
                            break;
                        }
                        case "excel file": {
                            String filePath = createScopedFilePath(pin, ".excel");
                            DownloadExtension.exportToExcel(list, filePath, AdminActivity.this);
                            break;
                        }
                    }
                }else {
                    Toast.makeText(AdminActivity.this, "Please Enter File Name", Toast.LENGTH_SHORT).show();
                }

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ipDialog.dismiss();
            }
        });

        ipDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

            }
        });
        ipDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        ipDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        ipDialog.setCancelable(false);
        ipDialog.show();
    }

    public String createScopedFilePath(String fileName, String fileExtension) {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "face_register_app_attendances");

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
    private List<AttendanceDBModel> getAllData(){
        List<AttendanceDBModel> attendanceDBModelList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("UsersInfo");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Iterate over all users
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userId = userSnapshot.getKey(); // Get user ID (UID)

                        // Get attendance data for this user
                        DataSnapshot attendanceSnapshot = userSnapshot.child("Attendance");

                        if (attendanceSnapshot.exists()) {
                            // Iterate over all attendance records for the current user
                            for (DataSnapshot attendanceDataSnapshot : attendanceSnapshot.getChildren()) {
                                AttendanceDBModel model = attendanceDataSnapshot.getValue(AttendanceDBModel.class);
                                attendanceDBModelList.add(model);
                                // Handle the model data here, e.g., save to local DB or process
                                Log.d("MyApp", "User ID: " + userId + " Attendance ID: " + model.getId());
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("MyApp", "Failed to load data: " + error.getMessage());
            }
        });
        return attendanceDBModelList;
    }

    public void listFilesInDirectory() {
        // Define the folder path
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "face_register_app_attendances");

        // Check if the directory exists and is a valid directory
        if (directory.exists() && directory.isDirectory()) {
            // List all files in the directory
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    // Check if it's a file or a directory
                    if (file.isFile()) {
                        // Log or process the file
                        String fileExtension = getFileExtension(file);
                        System.out.println("File: " + file.getName()+fileExtension);
                    } else if (file.isDirectory()) {
                        // Log or process the sub-directory
                        System.out.println("Sub-directory: " + file.getName());
                    }
                }
            }
        } else {
            System.out.println("Directory does not exist or is not a directory.");
        }
    }
    public String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");

        // Check if the file has an extension
        if (dotIndex > 0) {
            return fileName.substring(dotIndex + 1); // Get the file extension after the dot
        } else {
            return ""; // No extension
        }
    }

}