package com.example.face_recognition_attendance_app.Activities.Login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.face_recognition_attendance_app.Activities.Models.User;
import com.example.face_recognition_attendance_app.Activities.ScanUserFaceActivity;
import com.example.face_recognition_attendance_app.Activities.Util.FragmentUtils;
import com.example.face_recognition_attendance_app.Activities.enums.Role;
import com.example.face_recognition_attendance_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        getSupportActionBar().hide();
        FragmentUtils.SetFragment(getSupportFragmentManager(),new LoginFragment(),R.id.loginParentFrameLay);
//        User user = new User("zeeshan taj","1234567", Role.USER);
//        addNewUser(user);

    }

    private void addNewUser(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("MyApp", "Successfully added new employee");
                        String userId = documentReference.getId();

                        redirectToScanFaceActivity(userId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MyApp", "failed to add user: " + e.getMessage());
                    }
                });
    }

    private void redirectToScanFaceActivity(String userId) {
        Intent intent = new Intent(this, ScanUserFaceActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

}
