package com.roshan.android.picscheduler;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.util.ArrayList;
import java.util.Calendar;
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

    private List<Event> events;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detect);
        ButterKnife.bind(this);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeData();

//        RVAdapter adapter = new RVAdapter(events);
//        recyclerView.setAdapter(adapter);

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
                        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                            for (FirebaseVisionText.Line line : block.getLines()) {
                                String lineText = line.getText();
                                List<String> times = new ArrayList<>();
                                List<String> ampm = new ArrayList<>();
                                String scheduleDays = "test";
                                for (FirebaseVisionText.Element element : line.getElements()) {
                                    String elementText = element.getText();
                                    if (elementText.contains(":") && elementText.length() >= 4) {
                                        int firstIndex = elementText.indexOf(":");
                                        int firstEndIndex = firstIndex + 3;
                                        int secondIndex = elementText.lastIndexOf(":");
                                        int secondEndIndex = secondIndex + 3;
                                        if (firstEndIndex <= elementText.length()) {
                                            times.add(elementText.substring(0, firstEndIndex));
                                        }
                                        if (elementText.contains("-") && firstIndex != secondIndex && secondEndIndex <= elementText.length()) {
                                            int indexHyphen = elementText.indexOf("-");
                                            times.add(elementText.substring(indexHyphen + 1, secondEndIndex));
                                        }
                                    }
                                    if (elementText.contains("AM") || (elementText.contains(":") && elementText.contains("A"))) {
                                        ampm.add("AM");
                                    } else if (elementText.contains("PM") || (elementText.contains(":") && elementText.contains("P"))) {
                                        ampm.add("PM");
                                    }
                                    if (elementText.equals("MWF") || elementText.equals("M,W,F")) {
                                        scheduleDays = "MWF";
                                    } else if (elementText.equals("M") || elementText.equals("Mon") || elementText.contains("Monday")) {
                                        scheduleDays = "M";
                                    } else if (elementText.equals("W") || elementText.equals("Wed") || elementText.contains("Wednesday")) {
                                        scheduleDays = "W";
                                    } else if (elementText.equals("F") || elementText.equals("Fri") || elementText.contains("Friday")) {
                                        scheduleDays = "F";
                                    } else if (elementText.equals("Tu") || elementText.equals("Tues") || elementText.contains("Tuesday")) {
                                        scheduleDays = "Tu";
                                    } else if (elementText.equals("Th") || elementText.equals("Thur") || elementText.equals("Thurs") || elementText.contains("Thursday")) {
                                        scheduleDays = "Th";
                                    } else if (elementText.equals("TuTh") || elementText.equals("TR")) {
                                        scheduleDays = "TuTh";
                                    }
                                }
                                if (!times.isEmpty()) {
                                    if (times.size() == 2) {
                                        events.add(new  Event(scheduleDays, times.get(0), times.get(1)));
                                    } else if (times.size() == 1) {
                                        events.add(new Event(scheduleDays, times.get(0), "10:00"));
                                    }
                                }
                            }
                        }
                        RVAdapter adapter = new RVAdapter(events);
                        recyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
        createEvent(Calendar.SUNDAY);

//        result = detector.processImage(image)
//                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
//                    @Override
//                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
//                        String resultText = firebaseVisionText.getText();
//                        for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
//                            String blockText = block.getText();
//                            Float blockConfidence = block.getConfidence();
//                            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
//                            Point[] blockCornerPoints = block.getCornerPoints();
//                            Rect blockFrame = block.getBoundingBox();
//                            for (FirebaseVisionText.Line line : block.getLines()) {
//                                String lineText = line.getText();
//                                Float lineConfidence = line.getConfidence();
//                                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
//                                Point[] lineCornerPoints = line.getCornerPoints();
//                                Rect lineFrame = line.getBoundingBox();
//                                if (lineText.contains(":")) {
//                                    textView.append(lineText + "\n\n");
//                                    for (FirebaseVisionText.Element element : line.getElements()) {
//                                        String elementText = element.getText();
//                                    }
//                                }
//                            }
//                        }
//
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                    }
//                });
    }


    private void initializeData() {
        events = new ArrayList<>();
        events.add(new Event("Test 1", "start 1", "end 1"));
        events.add(new Event("Test 2", "start 2", "end 2"));
        events.add(new Event("Test 3", "start 3", "end 3"));
    }

    private void createEvent(int day) {
        Calendar now = Calendar.getInstance();
        int weekday = now.get(Calendar.DAY_OF_WEEK);
        if (weekday != day) {
            int days = (Calendar.SATURDAY - weekday + 7 - Math.abs(Calendar.SATURDAY - day)) % 7;
            now.add(Calendar.DAY_OF_YEAR, days);
        }
//        Calendar beginTime = Calendar.getInstance();
//        beginTime.set(2019, 1, 8, 7, 30);
//        Calendar endTime = Calendar.getInstance();
//        endTime.set(2019, 1, 8, 8, 30);
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, now.getTimeInMillis())
//                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.RRULE, "FREQ=WEEKLY;BYDAY=TU,TH")
                .putExtra(CalendarContract.Events.TITLE, "test")
                .putExtra(CalendarContract.Events.DESCRIPTION, "testing description");
        startActivity(intent);
    }

    class Event {
        String name;
        String start;
        String end;

        Event(String name, String start, String end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }
    }

}