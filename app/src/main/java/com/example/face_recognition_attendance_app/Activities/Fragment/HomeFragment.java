package com.example.face_recognition_attendance_app.Activities.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.face_recognition_attendance_app.databinding.FragmentSignUpBinding;
import com.example.face_recognition_attendance_app.databinding.HomeFragmentBinding;

public class HomeFragment extends Fragment {


    private HomeFragmentBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(inflater, container, false);



        return binding.getRoot();
    }
}
