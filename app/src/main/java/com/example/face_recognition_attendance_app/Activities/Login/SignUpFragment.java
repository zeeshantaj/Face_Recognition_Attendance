package com.example.face_recognition_attendance_app.Activities.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.face_recognition_attendance_app.Activities.HomeActivity;
import com.example.face_recognition_attendance_app.Activities.ScanUserFaceActivity;
import com.example.face_recognition_attendance_app.Activities.Util.FragmentUtils;
import com.example.face_recognition_attendance_app.R;
import com.example.face_recognition_attendance_app.databinding.FragmentSignUpBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpFragment extends Fragment {



    public SignUpFragment() {
        // Required empty public constructor
    }

    FragmentSignUpBinding binding;
    private FrameLayout parentFrameLayout;
    private FirebaseAuth auth;
    private ProgressDialog dialog;
    private static final String EMAIL_PATTERN =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private Uri imageUri;
    private UploadTask uploadTask;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference,imageRef;
    private String downloadedImageUri;
    private DatabaseReference databaseReference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        dialog = new ProgressDialog(getActivity());
        parentFrameLayout = getActivity().findViewById(R.id.loginParentFrameLay);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();


        databaseReference = FirebaseDatabase.getInstance().getReference();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        String imageUid = auth.getUid();

        String imageName = "image_"+imageUid+".jpg";

        imageRef = storageReference.child("UserImages/"+imageName);

        binding.loginText.setOnClickListener(view12 ->
                FragmentUtils.SetFragment(getActivity().getSupportFragmentManager(),new LoginFragment(),parentFrameLayout.getId()));

            binding.singUpBtn.setOnClickListener(view1 -> {
                String name = binding.SignUpName.getText().toString();
                String email = binding.SignUpEmail.getText().toString();
                String pass = binding.SignUpPass.getText().toString();
                String conPass = binding.SignUpConPass.getText().toString();

                if (email.isEmpty() && !email.matches(EMAIL_PATTERN)) {
                    binding.SignUpEmail.setError("Email should be in correct pattern and can not be empty");
                }
                if (name.isEmpty()) {
                    binding.SignUpName.setError("Name is Empty");
                }
                if (pass.isEmpty()) {
                    binding.SignUpPass.setError("password field is empty");

                }
                if (conPass.isEmpty()) {
                    binding.SignUpConPass.setError("password field is empty");
                }
                if (!pass.equals(conPass)) {
                    Toast.makeText(getActivity(), "Password and Confirm Password should be same", Toast.LENGTH_SHORT).show();
                }

                if (!name.isEmpty() && !email.isEmpty()
                        && !pass.isEmpty() && !conPass.isEmpty()) {
                    dialog.setTitle("Please Wait");
                    dialog.setMessage("Creating User....");
                    dialog.setCancelable(false);
                    dialog.show();

                    auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener(authResult -> {

                        HashMap<String,Object> value = new HashMap<>();
                        value.put("name",name);
                        value.put("email",email);
                        value.put("password",pass);
                        value.put("isRegistered", false);
                        String uid = auth.getUid();

                        databaseReference.child("UsersInfo").child(Objects.requireNonNull(uid)).setValue(value)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(getActivity(), "Account Created!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    redirectToScanFaceActivity(uid);
                                }).addOnFailureListener(e -> {
                                    dialog.dismiss();
                                    Toast.makeText(getActivity(), "Error "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    Log.d("MyApp", Objects.requireNonNull(e.getLocalizedMessage()));
                                });


                    }).addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("MyApp","error "+e.getMessage());
                        dialog.dismiss();
                    });

                }
            });
    }
    private void redirectToScanFaceActivity(String userId) {
        Intent intent = new Intent(getContext(), ScanUserFaceActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("isRegistered", false);
        startActivity(intent);
    }
}