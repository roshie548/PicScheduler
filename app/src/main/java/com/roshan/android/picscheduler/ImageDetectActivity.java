package com.roshan.android.picscheduler;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageDetectActivity extends AppCompatActivity {

    private static Intent intent;
    private static byte[] imageBytes;
    private static Bitmap bitmap;
    private static FirebaseVisionImage image;
    private static FirebaseVisionTextRecognizer detector;
    private static Task<FirebaseVisionText> result;
    private static int cameraWidth;
    private static int cameraHeight;

    @BindView(R.id.text_view) TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detect);
        ButterKnife.bind(this);

        intent = getIntent();
        imageBytes = intent.getByteArrayExtra("CapturedImage");
        cameraWidth = intent.getIntExtra("width", 0);
        cameraHeight = intent.getIntExtra("height", 0);

        bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        bitmap = Bitmap.createScaledBitmap(bitmap, cameraWidth, cameraHeight, false);

        image = FirebaseVisionImage.fromBitmap(bitmap);
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        result = detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        String resultText = firebaseVisionText.getText();
                        for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
                            String blockText = block.getText();
                            Float blockConfidence = block.getConfidence();
                            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                            Point[] blockCornerPoints = block.getCornerPoints();
                            Rect blockFrame = block.getBoundingBox();
                            for (FirebaseVisionText.Line line : block.getLines()) {
                                String lineText = line.getText();
                                Float lineConfidence = line.getConfidence();
                                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                                Point[] lineCornerPoints = line.getCornerPoints();
                                Rect lineFrame = line.getBoundingBox();
                                if (lineText.contains(":")) {
                                    textView.append(lineText + "\n\n");
                                }
                            }
                        }

//                        String resultText = firebaseVisionText.getText();
//                        Log.d("TEST", resultText);
//                        textView.setText(resultText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

}
