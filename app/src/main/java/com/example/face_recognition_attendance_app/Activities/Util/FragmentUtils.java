package com.example.face_recognition_attendance_app.Activities.Util;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentUtils {
    public static void SetFragment(FragmentManager fragmentManager, Fragment fragment, int ContainerID){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(ContainerID,fragment);
        fragmentTransaction.commit();
    }

}
