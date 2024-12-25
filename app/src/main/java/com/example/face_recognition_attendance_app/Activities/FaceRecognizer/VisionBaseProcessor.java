package com.example.face_recognition_attendance_app.Activities.FaceRecognizer;

import android.graphics.Bitmap;

import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;

public abstract class VisionBaseProcessor<T> {
    public abstract Task<T> detectInImage(ImageProxy imageProxy, Bitmap bitmap, int rotationDegrees);

    public abstract void stop();
}
