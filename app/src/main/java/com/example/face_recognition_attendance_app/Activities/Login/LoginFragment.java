package com.example.face_recognition_attendance_app.Activities.Login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.face_recognition_attendance_app.R;
import com.google.android.material.textfield.TextInputEditText;

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


        return view;

    }
}
