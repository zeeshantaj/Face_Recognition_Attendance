package com.example.face_recognition_attendance_app.Activities.Login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.face_recognition_attendance_app.Activities.HomeActivity;
import com.example.face_recognition_attendance_app.Activities.Util.FragmentUtils;
import com.example.face_recognition_attendance_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {



    public LoginFragment() {
        // Required empty public constructor
    }

    private View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_login, container, false);

        TextInputEditText name = view.findViewById(R.id.loginEmail);
        TextInputEditText password = view.findViewById(R.id.loginPass);
        Button login = view.findViewById(R.id.loginBtn);
        TextView resetPasswordTxt = view.findViewById(R.id.txtForgetPass);
        Button signupBtn = view.findViewById(R.id.signUpText);

        login.setOnClickListener(view1 -> {
            String strName = name.getText().toString().trim();
            String strPass = password.getText().toString().trim();
            if (strName.isEmpty()){
                name.setError("Name is empty!");
                return;
            }
            if (strPass.isEmpty()){
                name.setError("Password is empty!");
                return;
            }

            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(strName,strPass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Toast.makeText(getContext(), "Login success", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getContext(), HomeActivity.class));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        signupBtn.setOnClickListener(view1 -> {
            FragmentUtils.SetFragment(getActivity().getSupportFragmentManager(), new SignUpFragment(),R.id.loginParentFrameLay);
        });
        resetPasswordTxt.setOnClickListener(view1 -> {
            FragmentUtils.SetFragment(getActivity().getSupportFragmentManager(), new ResetPasswordFragment(),R.id.loginParentFrameLay);
        });

        return view;

    }
}
