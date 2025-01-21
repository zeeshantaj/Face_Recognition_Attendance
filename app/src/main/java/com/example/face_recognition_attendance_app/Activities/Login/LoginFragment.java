package com.example.face_recognition_attendance_app.Activities.Login;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.face_recognition_attendance_app.Activities.Activity.AdminActivity;
import com.example.face_recognition_attendance_app.Activities.HomeActivity;
import com.example.face_recognition_attendance_app.Activities.Util.FragmentUtils;
import com.example.face_recognition_attendance_app.Activities.Util.UiHelper;
import com.example.face_recognition_attendance_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mukeshsolanki.OtpView;

public class LoginFragment extends Fragment {



    public LoginFragment() {
        // Required empty public constructor
    }

    private View view;
    private ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_login, container, false);

        dialog = new ProgressDialog(getContext());
        TextInputEditText name = view.findViewById(R.id.loginEmail);
        TextInputEditText password = view.findViewById(R.id.loginPass);
        Button login = view.findViewById(R.id.loginBtn);
        TextView resetPasswordTxt = view.findViewById(R.id.txtForgetPass);
        Button signupBtn = view.findViewById(R.id.signUpText);
        Button admin = view.findViewById(R.id.adminBtn);

        login.setOnClickListener(view1 -> {
            String strName = name.getText().toString().trim();
            String strPass = password.getText().toString().trim();
            if (strName.isEmpty()){
                name.setError("Name is empty!");
                return;
            }
            if (strPass.isEmpty()){
                password.setError("Password is empty!");
                return;
            }

            dialog.setTitle("Please Wait");
            dialog.setMessage("Login in progress....");
            dialog.setCancelable(false);
            dialog.show();

            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(strName,strPass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Login success", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getContext(), HomeActivity.class));
                    getActivity().finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
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

        admin.setOnClickListener(view1 -> {
            enterConfigPin();
        });

        return view;

    }
    private void enterConfigPin() {
        final Dialog ipDialog = new Dialog(getContext());
        ipDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ipDialog.setContentView(R.layout.config_pin_dialog);
        ipDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button continueBtn = ipDialog.findViewById(R.id.enterBtn);
        Button cancelBtn = ipDialog.findViewById(R.id.cancelBtn);
        OtpView otpView = ipDialog.findViewById(R.id.otp_view);

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ipDialog.dismiss();
                String pin = otpView.getText().toString();
                if (!pin.isEmpty()){
                    if (pin.equals("2379")){
                        startActivity(new Intent(getContext(), AdminActivity.class));
                        ipDialog.dismiss();
                    }
                    else {
                        UiHelper.showFlawDialog(getContext(),"Error " ,"Please Enter correct pin",1);
                    }
                }else {
                    UiHelper.showFlawDialog(getContext(),"Error " ,"Please Enter pin",1);
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
}
