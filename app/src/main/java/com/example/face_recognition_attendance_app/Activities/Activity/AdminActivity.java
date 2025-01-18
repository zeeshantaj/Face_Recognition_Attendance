package com.example.face_recognition_attendance_app.Activities.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.face_recognition_attendance_app.R;

public class AdminActivity extends AppCompatActivity {

    CardView allUserCard,downloadAttCard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        allUserCard = findViewById(R.id.allUserCard);
        downloadAttCard = findViewById(R.id.downloadAllAttendanceCard);

        allUserCard.setOnClickListener(view -> {

        });
        downloadAttCard.setOnClickListener(view -> {

        });
    }
}