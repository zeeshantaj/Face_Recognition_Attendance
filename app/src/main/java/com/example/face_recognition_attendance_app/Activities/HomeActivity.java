package com.example.face_recognition_attendance_app.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.face_recognition_attendance_app.Activities.Adapter.NavAdapter;
import com.example.face_recognition_attendance_app.Activities.Fragment.AttendanceHistoryFragment;
import com.example.face_recognition_attendance_app.Activities.Fragment.HomeFragment;
import com.example.face_recognition_attendance_app.Activities.Fragment.ProfileFragment;
import com.example.face_recognition_attendance_app.Activities.Interfaces.OnCurrentLocationRetrieved;
import com.example.face_recognition_attendance_app.Activities.Login.LoginActivity;
import com.example.face_recognition_attendance_app.R;
import com.example.face_recognition_attendance_app.databinding.HomeActivityBinding;
import com.example.face_recognition_attendance_app.databinding.LoginActivityBinding;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    private HomeActivityBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private OnCurrentLocationRetrieved onCurrentLocationRetrieved;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = HomeActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new HomeFragment());
        fragments.add(new AttendanceHistoryFragment());
        fragments.add(new ProfileFragment());

        NavAdapter adapter = new NavAdapter(this, fragments);
        binding.viewPager.setAdapter(adapter);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                binding.viewPager.setCurrentItem(0, false);

            } else if (item.getItemId() == R.id.history) {
                binding.viewPager.setCurrentItem(1, false);

            } else if (item.getItemId() == R.id.profile) {
                binding.viewPager.setCurrentItem(2, false);

            }
            return true;
        });

        // Sync ViewPager with Bottom Navigation
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        binding.bottomNavigation.setSelectedItemId(R.id.home);
                        getSupportActionBar().setTitle("Attendance");
                        break;
                    case 1:
                        binding.bottomNavigation.setSelectedItemId(R.id.history);
                        getSupportActionBar().setTitle("History");
                        break;
                    case 2:
                        binding.bottomNavigation.setSelectedItemId(R.id.profile);
                        getSupportActionBar().setTitle("Profile");
                        break;
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLastLocation();
    }
    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (!isGPSEnabled()){
            promptEnableGPS();
        }else {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            if (onCurrentLocationRetrieved != null) {
                                onCurrentLocationRetrieved.onLocationRetrieved(latitude, longitude);
                            }
                            Log.d("Location", "Latitude: " + latitude + ", Longitude: " + longitude);
                        } else {
                            Log.d("Location", "Location not available");
                            if (onCurrentLocationRetrieved != null) {
                                onCurrentLocationRetrieved.onLocationFailed("Location not available");
                            }
                        }
                    }
                });
        }
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void promptEnableGPS() {
        new AlertDialog.Builder(this)
                .setTitle("Enable GPS")
                .setMessage("GPS is required for this app. Please enable GPS in settings.")
                .setPositiveButton("Enable", (dialog, which) -> {
                    // Open location settings
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Handle if the user cancels the dialog
                    Toast.makeText(this, "GPS is required for this feature.", Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();
    }
    public void setOnActivityEventListener(OnCurrentLocationRetrieved listener) {
        this.onCurrentLocationRetrieved = listener;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        }
        else {
            Toast.makeText(this, "Allow permission to mark attendance ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logOutMenu){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
