package com.example.face_recognition_attendance_app.Activities.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.face_recognition_attendance_app.Activities.Connectivity.HttpWeb;
import com.example.face_recognition_attendance_app.Activities.Models.AttendanceDBModel;
import com.example.face_recognition_attendance_app.Activities.SQLite.SqliteHelper;
import com.example.face_recognition_attendance_app.Activities.Util.UiHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.ConnectException;
import java.util.List;

public class UploadAttendanceToFirebase extends BroadcastReceiver {
    SqliteHelper sqliteHelper;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        sqliteHelper = new SqliteHelper(context);
        if ("uploadToData".equals(action)) {
            String data = intent.getStringExtra("key");
            Toast.makeText(context, "Received: " + data, Toast.LENGTH_SHORT).show();
            if (HttpWeb.isConnectingToInternet(context)){
                uploadData(context);
            }
        }
    }
    private void uploadData(Context context){
        List<AttendanceDBModel> attendanceDBModelList ;

        attendanceDBModelList = sqliteHelper.getAllAttendance();
        for (AttendanceDBModel att:attendanceDBModelList){
            uploadToFirebase(att,context);
        }
    }
    private void uploadToFirebase(AttendanceDBModel model, Context context){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("UsersInfo")
                .child(uid)
                .child("Attendance")
                .child(model.getId());
        databaseReference.setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                deleteFromDb();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteFromDb(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("UsersInfo")
                .child(uid)
                .child("Attendance");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        String id = snapshot1.child("id").getValue(String.class);
                        List<String> allIds = sqliteHelper.getAllIds();
                        for (String dbId : allIds){
                            if (id.equals(dbId)){
                                sqliteHelper.deleteEntryById(id);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("MyApp","error "+error.getMessage());
            }
        });
    }
}
