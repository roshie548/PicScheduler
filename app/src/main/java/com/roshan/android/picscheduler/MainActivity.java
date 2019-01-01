package com.roshan.android.picscheduler;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.camera) CameraKitView mCameraView;
    @BindView(R.id.camera_button) FloatingActionButton mCameraButton;
//    @BindView(R.id.camera_button) Button mCameraButton;

    private byte[] imageBytes;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mCameraView.setCameraListener(new CameraKitView.CameraListener() {
            @Override
            public void onOpened() {

            }

            @Override
            public void onClosed() {

            }
        });

        mCameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mCameraView.captureImage(new CameraKitView.ImageCallback() {
                    @Override
                    public void onImage(CameraKitView cameraKitView, byte[] bytes) {
                        imageBytes = bytes;

                        Intent intent = new Intent(MainActivity.this.getApplicationContext(), ImageDetectActivity.class);
                        intent.putExtra("CapturedImage", imageBytes);
                        intent.putExtra("width", mCameraView.getWidth());
                        intent.putExtra("height", mCameraView.getHeight());
                        startActivity(intent);
                    }
                });

                //TODO: Launch new activity, pass the bitmap
                // Maybe use Fragments instead of new activity?
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCameraView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
    }

    @Override
    protected void onPause() {
        mCameraView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mCameraView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mCameraView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
