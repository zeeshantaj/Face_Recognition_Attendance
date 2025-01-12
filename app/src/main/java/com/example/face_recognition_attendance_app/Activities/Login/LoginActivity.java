package com.example.face_recognition_attendance_app.Activities.Login;

import static java.security.AccessController.getContext;

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

import com.example.face_recognition_attendance_app.Activities.FaceRegistrationActivity;
import com.example.face_recognition_attendance_app.Activities.HomeActivity;
import com.example.face_recognition_attendance_app.Activities.Models.User;

import com.example.face_recognition_attendance_app.Activities.Util.FragmentUtils;
import com.example.face_recognition_attendance_app.Activities.Util.UiHelper;
import com.example.face_recognition_attendance_app.Activities.enums.Role;
import com.example.face_recognition_attendance_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        getSupportActionBar().hide();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null){
            checkIfFaceRegistered(auth.getUid());
        }else{
            FragmentUtils.SetFragment(getSupportFragmentManager(),new LoginFragment(),R.id.loginParentFrameLay);
        }
    }
    private void checkIfFaceRegistered(String uid){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("UsersInfo")
                .child(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                boolean isRegistered = snapshot.child("isRegistered").getValue(Boolean.class);
                if (isRegistered){
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                }else {
                    startActivity(new Intent(LoginActivity.this, FaceRegistrationActivity.class));
                }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                UiHelper.showFlawDialog(LoginActivity.this,"Error",error.getMessage(),1);
            }
        });
    }


}
