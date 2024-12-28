package com.example.face_recognition_attendance_app.Activities.FaceRecognizer.Recognizer;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;
import com.example.face_recognition_attendance_app.Activities.FaceRecognizer.FaceGraphic;
import com.example.face_recognition_attendance_app.Activities.FaceRecognizer.GraphicOverlay;
import com.example.face_recognition_attendance_app.Activities.FaceRecognizer.VisionBaseProcessor;
import com.example.face_recognition_attendance_app.Activities.ScanUserFaceActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanUserFaceProcessor extends VisionBaseProcessor<List<Face>> {
    class Employee {
        public String name;
        public float[] faceVector;

        public Employee(String name, float[] faceVector) {
            this.name = name;
            this.faceVector = faceVector;
        }
    }

    public interface ScanUserFaceCallback {
        void onFaceRecognised(Face face, float probability, String name);
        void onFaceDetected(Face face, Bitmap faceBitmap, float[] vector);
        void onVerificationComplete();
    }

    private static final String TAG = "FaceRecognitionProcessor";

    // Input image size for our facenet model
    private static final int FACENET_INPUT_IMAGE_SIZE = 112;

    private final FaceDetector detector;
    private final Interpreter faceNetModelInterpreter;
    private final ImageProcessor faceNetImageProcessor;
    private final GraphicOverlay graphicOverlay;
    private final ScanUserFaceCallback callback;

    public ScanUserFaceActivity activity;

    List<Employee> recognisedFaceList = new ArrayList<>();

    public ScanUserFaceProcessor(Interpreter faceNetModelInterpreter,
                                 GraphicOverlay graphicOverlay,
                                 ScanUserFaceCallback callback) {
        this.callback = callback;
        this.graphicOverlay = graphicOverlay;
        // initialize processors
        this.faceNetModelInterpreter = faceNetModelInterpreter;
        faceNetImageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(FACENET_INPUT_IMAGE_SIZE, FACENET_INPUT_IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0f, 255f))
                .build();

        FaceDetectorOptions faceDetectorOptions = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                // to ensure we don't count and analyse same person again
                .enableTracking()
                .build();
        detector = FaceDetection.getClient(faceDetectorOptions);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    public Task<List<Face>> detectInImage(ImageProxy imageProxy, Bitmap bitmap, int rotationDegrees) {
        InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(), rotationDegrees);
        int rotation = rotationDegrees;

        // In order to correctly display the face bounds, the orientation of the analyzed
        // image and that of the viewfinder have to match. Which is why the dimensions of
        // the analyzed image are reversed if its rotation information is 90 or 270.
        boolean reverseDimens = rotation == 90 || rotation == 270;
        int width;
        int height;
        if (reverseDimens) {
            width = imageProxy.getHeight();
            height =  imageProxy.getWidth();
        } else {
            width = imageProxy.getWidth();
            height = imageProxy.getHeight();
        }
        return detector.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        graphicOverlay.clear();
                        for (Face face : faces) {
                            FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay, face, false, width, height);
                            Log.d(TAG, "face found, id: " + face.getTrackingId());
//                            if (activity != null) {
//                                activity.setTestImage(cropToBBox(bitmap, face.getBoundingBox(), rotation));
//                            }
                            // now we have a face, so we can use that to analyse age and gender
                            Bitmap faceBitmap = cropToBBox(bitmap, face.getBoundingBox(), rotation);

                            if (faceBitmap == null) {
                                Log.d("GraphicOverlay", "Face bitmap null");
                                return;
                            }

                            TensorImage tensorImage = TensorImage.fromBitmap(faceBitmap);
                            ByteBuffer faceNetByteBuffer = faceNetImageProcessor.process(tensorImage).getBuffer();
                            float[][] faceOutputArray = new float[1][192];
                            faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray);

                            Log.d(TAG, "output array: " + Arrays.deepToString(faceOutputArray));

                            if (callback != null) {
                                callback.onFaceDetected(face, faceBitmap, faceOutputArray[0]);
                                if (!recognisedFaceList.isEmpty()) {
                                    Pair<String, Float> result = findNearestFace(faceOutputArray[0]);
                                    // if distance is within confidence
                                    if (result.second < 1.0f) {
                                        faceGraphic.name = result.first;
                                        callback.onFaceRecognised(face, result.second, result.first);
                                    }
                                }
                            }

                            graphicOverlay.add(faceGraphic);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Log.d("MyApp","exception "+e.getMessage());
                        // intentionally left empty
                    }
                });
    }

    // looks for the nearest vector in the dataset (using L2 norm)
    // and returns the pair <name, distance>
    private Pair<String, Float> findNearestFace(float[] vector) {

        Pair<String, Float> ret = null;
        for (Employee user : recognisedFaceList) {
            final String name = user.name;
            final float[] knownVector = user.faceVector;

            float distance = 0;
            for (int i = 0; i < vector.length; i++) {
                float diff = vector[i] - knownVector[i];
                distance += diff*diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                ret = new Pair<>(name, distance);
            }
        }

        return ret;
    }

    public void stop() {
        detector.close();
    }

    private Bitmap cropToBBox(Bitmap image, Rect boundingBox, int rotation) {
        int shift = 0;
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
        }
        if (boundingBox.top >= 0 && boundingBox.bottom <= image.getWidth()
                && boundingBox.top + boundingBox.height() <= image.getHeight()
                && boundingBox.left >= 0
                && boundingBox.left + boundingBox.width() <= image.getWidth()) {
            return Bitmap.createBitmap(
                    image,
                    boundingBox.left,
                    boundingBox.top + shift,
                    boundingBox.width(),
                    boundingBox.height()
            );
        } else return null;
    }

    // Register a name against the vector
    public void registerFace(String userId, float[] tempVector,Bitmap bitmap) {
        // Convert float array to string
        String faceVectorString = convertFloatArrayToString(tempVector);

        // Add new Employee to recognisedFaceList
        recognisedFaceList.add(new Employee(userId, tempVector));


        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("UsersInfo")
                .child(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("face_vector", faceVectorString);

        databaseReference.updateChildren(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                recognisedFaceList.clear();
                uploadImageToDb(bitmap,userId);
                Log.d(TAG, "Document added successfully to database for userID: " + userId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.w(TAG, "Error adding vectorFace", e);
            }
        });
//        db.collection("Users")
//                .document(userId)
//                .update(data)
//                .addOnSuccessListener(documentReference -> {
//                    Log.d(TAG, "Document added successfully to Firestore for userID: " + userId);
//                    recognisedFaceList.clear();
//                })
//                .addOnFailureListener(e -> {
//                    db.collection("Users").document(userId).delete();
//                    Log.w(TAG, "Error adding document to Firestore", e);
//                });
    }
    public void uploadImageToDb(Bitmap bitmap,String userId){
        if (bitmap == null) {
            Log.e("BitmapUploader", "Bitmap is null");
            return;
        }

        // Step 1: Convert Bitmap to Byte Array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // Step 2: Upload Byte Array to Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/UserScanImage/"+userId+"/"+userId+".png");

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // File uploaded successfully
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("UsersInfo")
                        .child(userId);
                HashMap<String,Object> value = new HashMap<>();
                value.put("imageUrl", uri.toString());
                databaseReference.updateChildren(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.v("MyApp","everything uploaded to firebase goto home");
                        callback.onVerificationComplete();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("image url upload to db failed", "image url upload to db failed", e);
                    }
                });
            });
        }).addOnFailureListener(e -> {
            // Failed to upload
            Log.e("BitmapUploader", "Failed to upload bitmap to Firebase Storage", e);
        });
    }
    public static String convertFloatArrayToString(float[] floatArray) {
        StringBuilder stringBuilder = new StringBuilder();
        for (float value : floatArray) {
            stringBuilder.append(value).append(",");
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }
}
