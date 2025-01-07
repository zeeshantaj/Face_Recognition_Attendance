package com.example.face_recognition_attendance_app.Activities.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.face_recognition_attendance_app.Activities.Adapter.HistoryAdapter;
import com.example.face_recognition_attendance_app.Activities.Models.AttendanceHistoryModel;
import com.example.face_recognition_attendance_app.databinding.AttendanceHistoryFragmentBinding;
import com.example.face_recognition_attendance_app.databinding.FragmentSignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AttendanceHistoryFragment extends Fragment {

    private AttendanceHistoryFragmentBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AttendanceHistoryFragmentBinding.inflate(inflater, container, false);

        getData();

        return binding.getRoot();
    }
    private void getData(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("UsersInfo")
                .child(uid)
                .child("Attendance");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<AttendanceHistoryModel> modelList = new ArrayList<>();
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        AttendanceHistoryModel model = dataSnapshot.getValue(AttendanceHistoryModel.class);
                        modelList.add(model);
                    }
                    HistoryAdapter adapter = new HistoryAdapter(modelList);
                    binding.historyRV.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.historyRV.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
