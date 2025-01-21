package com.example.face_recognition_attendance_app.Activities.Fragment;

import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.face_recognition_attendance_app.Activities.Adapter.HistoryAdapter;
import com.example.face_recognition_attendance_app.Activities.Connectivity.HttpWeb;
import com.example.face_recognition_attendance_app.Activities.Models.AttendanceDBModel;
import com.example.face_recognition_attendance_app.Activities.Models.AttendanceHistoryModel;
import com.example.face_recognition_attendance_app.Activities.SQLite.SqliteHelper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttendanceHistoryFragment extends Fragment {

    private AttendanceHistoryFragmentBinding binding;
    SqliteHelper sqliteHelper;
    List<AttendanceDBModel> modelList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AttendanceHistoryFragmentBinding.inflate(inflater, container, false);

        sqliteHelper = new SqliteHelper(getContext());
        modelList = sqliteHelper.getAllAttendanceFromFirebase();
        if (HttpWeb.isConnectingToInternet(getContext())){
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(this::getData);
        }else {
            loadLocalData();
        }
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

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
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        List<String> existingIds = sqliteHelper.getAllFirebaseIds();
                        AttendanceDBModel model = dataSnapshot.getValue(AttendanceDBModel.class);
                        if (model != null && !existingIds.contains(model.getId())) {
                            sqliteHelper.addAttendanceFromFirebase(model);
                        }
                    }
                    loadLocalData();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadLocalData(){
        modelList = sqliteHelper.getAllAttendanceFromFirebase();
        HistoryAdapter adapter = new HistoryAdapter(modelList);
        binding.historyRV.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.historyRV.setAdapter(adapter);    }
}
