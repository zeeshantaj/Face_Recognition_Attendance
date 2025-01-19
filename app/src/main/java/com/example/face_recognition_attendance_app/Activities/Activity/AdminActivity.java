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
                    if (selectedText.equals("word file")){
                        String filePath = createScopedFilePath(pin, ".word");
                        DownloadExtension.exportToWord(list,filePath,AdminActivity.this);
                    }else if (selectedText.equals("pdf file")){
                        String filePath = createScopedFilePath(pin, ".pdf");
                        DownloadExtension.exportToPDF(list,filePath,AdminActivity.this);
                    }
                    else if (selectedText.equals("excel file")){
                        String filePath = createScopedFilePath(pin, ".excel");
                        DownloadExtension.exportToExcel(list,filePath,AdminActivity.this);
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