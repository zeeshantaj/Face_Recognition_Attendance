package com.example.face_recognition_attendance_app.Activities;

import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.face_recognition_attendance_app.Activities.Adapter.NavAdapter;
import com.example.face_recognition_attendance_app.Activities.Fragment.AttendanceHistoryFragment;
import com.example.face_recognition_attendance_app.Activities.Fragment.HomeFragment;
import com.example.face_recognition_attendance_app.Activities.Fragment.ProfileFragment;
import com.example.face_recognition_attendance_app.R;
import com.example.face_recognition_attendance_app.databinding.HomeActivityBinding;
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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = HomeActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new HomeFragment());
        fragments.add(new AttendanceHistoryFragment());
        fragments.add(new ProfileFragment());

        NavAdapter adapter = new NavAdapter(this,fragments);
        binding.viewPager.setAdapter(adapter);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home){
                binding.viewPager.setCurrentItem(0, false);

            }
            else if (item.getItemId() == R.id.history){
                binding.viewPager.setCurrentItem(1, false);

            }
            else if (item.getItemId() == R.id.profile){
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
                        break;
                    case 1:
                        binding.bottomNavigation.setSelectedItemId(R.id.history);
                        break;
                    case 2:
                        binding.bottomNavigation.setSelectedItemId(R.id.profile);
                        break;
                }
            }
        });
    }

}
