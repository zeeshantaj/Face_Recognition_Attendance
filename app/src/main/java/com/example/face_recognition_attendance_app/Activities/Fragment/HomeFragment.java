package com.example.face_recognition_attendance_app.Activities.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.face_recognition_attendance_app.Activities.FaceRegistrationActivity;
import com.example.face_recognition_attendance_app.Activities.HomeActivity;
import com.example.face_recognition_attendance_app.Activities.Interfaces.OnCurrentLocationRetrieved;
import com.example.face_recognition_attendance_app.Activities.SQLite.SqliteHelper;
import com.example.face_recognition_attendance_app.Activities.Util.UiHelper;
import com.example.face_recognition_attendance_app.R;
import com.example.face_recognition_attendance_app.databinding.HomeFragmentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeFragment extends Fragment implements OnCurrentLocationRetrieved {


    private HomeFragmentBinding binding;
    // ubit location
//    private static final double TARGET_LAT = 24.940664;
//    private static final double TARGET_LON = 67.123947;
    // home location
    private static final double TARGET_LAT = 24.896131;
    private static final double TARGET_LON = 67.014410;
    private static final float RADIUS_IN_METERS = 500; // 500 meters radius

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(inflater, container, false);

        binding.checkInCard.setEnabled(false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof HomeActivity) {
            HomeActivity activity = (HomeActivity) context;
            // Register the fragment as a listener
            activity.setOnActivityEventListener(this);
        } else {
            throw new RuntimeException("fragment must be an instance of MyActivity");
        }
    }

    @Override
    public void onLocationRetrieved(double lat, double lon) {

        Log.d("MainActivity", "Location Received: Latitude = " + lat + ", Longitude = " + lon);

        // Calculate distance from target
        float[] results = new float[1];
        Location.distanceBetween(lat, lon, TARGET_LAT, TARGET_LON, results);

        float distanceInMeters = results[0];
        Log.d("MainActivity", "Distance to Target: " + distanceInMeters + " meters");

        if (distanceInMeters <= RADIUS_IN_METERS) {
            Log.d("MainActivity", "User is within the defined radius!");
            sentToFaceRecognition();
        } else {
            binding.checkIn.setText("out of radius");
            binding.checkInCard.setEnabled(false);
            binding.checkInCard.setBackgroundResource(R.drawable.red_circle);
            binding.locationStatementTxt.setText("You are outside Attendance radius!");
            Log.d("MainActivity", "User is outside the defined radius!");
        }
    }

    @Override
    public void onLocationFailed(String message) {
        UiHelper.showFlawDialog(getContext(), "Error", message, 1);
    }

    private void sentToFaceRecognition() {

        binding.locationStatementTxt.setText("You are within Attendance radius!");

        binding.checkInCard.setEnabled(true);
        binding.checkInCard.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), FaceRegistrationActivity.class);
            intent.putExtra("isRegistered", true);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        isCheckIn();
    }

    private void isCheckIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getUid();
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
//                .getReference()
//                .child("UsersInfo")
//                .child(uid);
//        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                isCheckIn = snapshot.child("isCheckIn").getValue(Boolean.class);
//                if (!isCheckIn){
//                    binding.checkInCard.setBackgroundResource(R.drawable.green_circle);
//                    binding.checkIn.setText("CheckIn");
//                    sentToFaceRecognition();
//                }else {
//                    binding.checkInCard.setBackgroundResource(R.drawable.green_circle);
//                    binding.checkIn.setText("CheckOut");
//                    sentToFaceRecognition();
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                UiHelper.showFlawDialog(getContext(),"Error",error.getMessage(),1);
//            }
//        });
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
        String randomId = sharedPreferences.getString("currentRandomId", null);

        SqliteHelper helper = new SqliteHelper(getContext());
        boolean isCheckIn = false;
        if (randomId != null){
            isCheckIn = helper.checkAttendanceExists(randomId, getTodaysDate());
        }
        binding.checkInCard.setBackgroundResource(R.drawable.green_circle);
        if (!isCheckIn) {
            binding.checkIn.setText("CheckIn");
        } else {
            binding.checkIn.setText("CheckOut");
        }
        sentToFaceRecognition();
    }


    private String getTodaysDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy");
        Date currentDate = new Date();
        return sdf.format(currentDate);
    }
}
