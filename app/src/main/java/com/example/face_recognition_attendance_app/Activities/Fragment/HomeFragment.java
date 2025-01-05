package com.example.face_recognition_attendance_app.Activities.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.face_recognition_attendance_app.Activities.FaceRegistrationActivity;
import com.example.face_recognition_attendance_app.databinding.HomeFragmentBinding;

public class HomeFragment extends Fragment {


    private HomeFragmentBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(inflater, container, false);

        binding.checkInCard.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), FaceRegistrationActivity.class);
            intent.putExtra("isRegistered",true);
            startActivity(intent);
        });


        return binding.getRoot();
    }
}
