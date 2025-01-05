package com.example.face_recognition_attendance_app.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.face_recognition_attendance_app.Activities.Util.UiHelper;
import com.example.face_recognition_attendance_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FaceRegistrationActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 1001;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    protected PreviewView previewView;
    private ImageAnalysis imageAnalysis;
    private FrameLayout videoFrameLayout;
    private ImageCapture imageCapture;
    private ImageButton captureBtn;
    private boolean isRegistered;
    private TextView instructionTxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_registration);
        getSupportActionBar().hide();

        cameraProviderFuture = ProcessCameraProvider.getInstance(getApplicationContext());
        previewView = findViewById(R.id.camera_source_preview);
        videoFrameLayout = findViewById(R.id.frameLay);
        captureBtn = findViewById(R.id.captureBtn);
        instructionTxt = findViewById(R.id.instructionTxt);

        Intent intent = getIntent();
        isRegistered = intent.getBooleanExtra("isRegistered",false);
        if (isRegistered){
            instructionTxt.setText("Place your face within the circular frame and capture an image to mark attendance.");

        }else {
            instructionTxt.setText("Place your face within the circular frame and capture an image to register your face.");
        }

        final float cornerRadius = 360f; // Adjust the radius as per your need
        videoFrameLayout.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
            }
        });
        videoFrameLayout.setClipToOutline(true); // Enable clipping to the outline


        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        } else {
            initSource();
        }
    }

    private void initSource() {

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(getApplicationContext()));
    }


    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        int lensFacing = getLensFacing();
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build();

        imageCapture = new ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview,imageCapture);

        captureBtn.setOnClickListener(view -> {

            captureImage();
        });
    }
    public void captureImage() {
        // Create an ImageCapture output options for in-memory image capture
        ImageCapture.OnImageCapturedCallback capturedCallback = new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                super.onCaptureSuccess(image);
                Bitmap bitmap = imageToBitmap(image);
                float[] currentEmbedding = extractFaceEmbedding(bitmap);
                List<Float> embeddingList = convertEmbeddingToList(currentEmbedding);

                // face already registered
                if (!isRegistered){
                    Map<String, Object> data = new HashMap<>();
                    data.put("embedding", embeddingList);
                    data.put("isRegistered", true);

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("UsersInfo")
                            .child("ZGtgySxgsAZtOt8jcXs70b3CBlR2");

                    databaseReference.updateChildren(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(FaceRegistrationActivity.this, "face registered successfully ", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(FaceRegistrationActivity.this,HomeActivity.class));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
                }else {
                    //
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("UsersInfo")
                            .child("ZGtgySxgsAZtOt8jcXs70b3CBlR2");
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String name = dataSnapshot.child("name").getValue(String.class);
                                List<Double> embeddingList = (List<Double>) dataSnapshot.child("embedding").getValue();

                                // Convert the List<Double> to a float[]
                                float[] embedding = new float[embeddingList.size()];
                                for (int i = 0; i < embeddingList.size(); i++) {
                                    embedding[i] = embeddingList.get(i).floatValue();
                                }

                                // Compare the embedding here
                                float similarity = compareEmbeddings(currentEmbedding, embedding);
                                if (similarity > 0.7) { // Adjust the threshold as needed
                                    Log.d("FaceMatch", "Faces are similar with a similarity of: " + similarity);
                                    UiHelper.showFlawDialog(FaceRegistrationActivity.this,"Match found","Face matched!",3);
                                } else {
                                    UiHelper.showFlawDialog(FaceRegistrationActivity.this,"Match not found","Face didn't match!\ntry again",3);
                                    Log.d("FaceMatch", "Faces are not similar. Similarity: " + similarity);
                                }
                                Log.d("Comparison", "User: " + name + ", Similarity: " + similarity);
//                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("FirebaseError", "Error retrieving data", databaseError.toException());
                        }
                    });
                }

                image.close();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
                Log.e("Capture", "Image capture failed: " + exception.getMessage());
            }
        };

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), capturedCallback);
    }
    private float compareEmbeddings(float[] embedding1, float[] embedding2) {
        if (embedding1.length != embedding2.length) {
            throw new IllegalArgumentException("Embeddings must have the same size.");
        }

        float dotProduct = 0;
        float normA = 0;
        float normB = 0;

        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            normA += embedding1[i] * embedding1[i];
            normB += embedding2[i] * embedding2[i];
        }

        normA = (float) Math.sqrt(normA);
        normB = (float) Math.sqrt(normB);

        return dotProduct / (normA * normB);
    }

    private List<Float> convertEmbeddingToList(float[] embedding) {
        List<Float> embeddingList = new ArrayList<>();
        for (float value : embedding) {
            embeddingList.add(value);
        }
        return embeddingList;
    }
    private Bitmap preprocessImage(Bitmap bitmap) {
        // Resize image to the input size required by the model (e.g., 112x112)
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 112, 112, true);
        return resizedBitmap;
    }
    private float[] extractFaceEmbedding(Bitmap bitmap) {
        // Load the TensorFlow Lite model
        try {
            Interpreter.Options tfliteOptions = new Interpreter.Options();

            Interpreter tflite = new Interpreter(loadModelFile(), tfliteOptions);

            // Preprocess the image
            Bitmap processedBitmap = preprocessImage(bitmap);

            // Convert the Bitmap to a ByteBuffer for input to the model
            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * 112 * 112 * 3);
            inputBuffer.order(ByteOrder.nativeOrder());
            int[] intValues = new int[112 * 112];
            processedBitmap.getPixels(intValues, 0, 112, 0, 0, 112, 112);

            // Normalize the pixel values if needed (e.g., dividing by 255)
            for (int pixelValue : intValues) {
                inputBuffer.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f);  // Red
                inputBuffer.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f);   // Green
                inputBuffer.putFloat((pixelValue & 0xFF) / 255.0f);          // Blue
            }

            // Prepare the output buffer for embeddings
            float[][] embeddings = new float[1][192];

            // Run the inference
            tflite.run(inputBuffer, embeddings);

            return embeddings[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new float[0];  // Empty array if the extraction fails
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("mobile_face_net.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    // Convert the ImageProxy (from the camera capture) to Bitmap
    private Bitmap imageToBitmap(ImageProxy image) {
        int format = image.getFormat();

        // Handle JPEG format
        if (format == ImageFormat.JPEG) {
            // Get the buffer containing the JPEG data
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] jpegData = new byte[buffer.remaining()];
            buffer.get(jpegData);

            // Decode the JPEG byte array into a Bitmap
            return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
        } else {
            // Handle unsupported formats
            throw new IllegalArgumentException("Unsupported image format: " + format);
        }
    }
    protected int getLensFacing() {
        return CameraSelector.LENS_FACING_FRONT;
    }

}