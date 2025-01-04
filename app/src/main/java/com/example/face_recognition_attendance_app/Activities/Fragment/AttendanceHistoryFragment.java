package com.example.face_recognition_attendance_app.Activities.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.face_recognition_attendance_app.databinding.AttendanceHistoryFragmentBinding;
import com.example.face_recognition_attendance_app.databinding.FragmentSignUpBinding;

public class AttendanceHistoryFragment extends Fragment {

    private AttendanceHistoryFragmentBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AttendanceHistoryFragmentBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
}
