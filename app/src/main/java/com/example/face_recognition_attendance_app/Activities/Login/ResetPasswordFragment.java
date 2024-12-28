package com.example.face_recognition_attendance_app.Activities.Login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.face_recognition_attendance_app.Activities.Util.FragmentUtils;
import com.example.face_recognition_attendance_app.R;
import com.example.face_recognition_attendance_app.databinding.FragmentResetPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordFragment extends Fragment {



    public ResetPasswordFragment() {
        // Required empty public constructor
    }
    private FragmentResetPasswordBinding binding;
    private FrameLayout parentFrameLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding  = FragmentResetPasswordBinding.inflate(inflater, container, false);


        //backLogin = view.findViewById(R.id.backLoginText);
        parentFrameLayout = getActivity().findViewById(R.id.loginParentFrameLay);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        binding.loginText.setOnClickListener(view -> FragmentUtils.SetFragment(getActivity().getSupportFragmentManager(),new LoginFragment(),parentFrameLayout.getId()));

        binding.resetBtn.setOnClickListener(v -> {
            String email = binding.resetPassEd.getText().toString();
            binding.resetBtn.setEnabled(false);
            if (email.isEmpty()){
                binding.resetPassEd.setError("Email is empty");
                binding.resetBtn.setEnabled(true);
                return;
            }
            auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    binding.resetBtn.setEnabled(false);
                    binding.checkIBTxt.setVisibility(View.VISIBLE);
                }
                else {
                    binding.resetBtn.setEnabled(true);
                    Toast.makeText(getActivity(), "Error "+task.getException(), Toast.LENGTH_SHORT).show();
                }
            });

        });
        return binding.getRoot();

    }
}
