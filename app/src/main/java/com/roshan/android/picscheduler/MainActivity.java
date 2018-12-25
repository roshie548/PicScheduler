package com.roshan.android.picscheduler;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.text.TextRecognizer;
import com.roshan.android.picscheduler.camera.CameraSource;
import com.roshan.android.picscheduler.camera.CameraSourcePreview;
import com.roshan.android.picscheduler.camera.GraphicOverlay;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int RC_HANDLE_GMS = 9001;

    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay<OcrGraphic> graphicOverlay;

    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    private static final int CAMERA_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview = (CameraSourcePreview) findViewById(R.id.preview);
        graphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);

        boolean autoFocus = true;
        boolean useFlash = false;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
            return;
        }
    }

    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(graphicOverlay));

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available");

            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;


            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        cameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(30.0f)
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO : null)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null) {
            preview.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preview != null) {
            preview.release();
        }
    }

    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }
}
