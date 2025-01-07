package com.example.face_recognition_attendance_app.Activities.Interfaces;

public interface OnCurrentLocationRetrieved {
    void onLocationRetrieved(double lat,double lon);
    void onLocationFailed(String message);

}
