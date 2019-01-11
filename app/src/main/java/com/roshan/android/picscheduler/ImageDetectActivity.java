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
                        //Find blocks of text
                        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                            //Find lines in the blocks
                            for (FirebaseVisionText.Line line : block.getLines()) {
                                String lineText = line.getText();

                                //Holds the times of an event
                                List<String> times = new ArrayList<>();

                                //Holds whether the times are AM or PM
                                List<Integer> ampm = new ArrayList<>();

                                //Placeholder for days of the events
                                List<Integer> scheduleDays = new ArrayList<>();

                                //Find elements in the lines
                                for (FirebaseVisionText.Element element : line.getElements()) {
                                    String elementText = element.getText();

                                    //Identify the times and add them to the times ArrayList
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

                                    //Identifies AM and PM and add to the ampm ArrayList
                                    if (elementText.contains("AM") || (elementText.contains(":") && elementText.contains("A"))) {
                                        ampm.add(Calendar.AM);
                                    } else if (elementText.contains("PM") || (elementText.contains(":") && elementText.contains("P"))) {
                                        ampm.add(Calendar.PM);
                                    }

                                    //Identify which days and set it equal to the scheduleDays String
                                    //TBH this is ugly code :(
                                    if (elementText.equals("MWF") || elementText.equals("M,W,F")) {
                                        scheduleDays.add(Calendar.MONDAY);
                                        scheduleDays.add(Calendar.WEDNESDAY);
                                        scheduleDays.add(Calendar.FRIDAY);
                                    } else if (elementText.equals("M") || elementText.equals("Mon") || elementText.contains("Monday")) {
                                        scheduleDays.add(Calendar.MONDAY);
                                    } else if (elementText.equals("W") || elementText.equals("Wed") || elementText.contains("Wednesday")) {
                                        scheduleDays.add(Calendar.WEDNESDAY);
                                    } else if (elementText.equals("F") || elementText.equals("Fri") || elementText.contains("Friday")) {
                                        scheduleDays.add(Calendar.FRIDAY);
                                    } else if (elementText.equals("Tu") || elementText.equals("Tues") || elementText.contains("Tuesday")) {
                                        scheduleDays.add(Calendar.TUESDAY);
                                    } else if (elementText.equals("Th") || elementText.equals("Thur") || elementText.equals("Thurs") || elementText.contains("Thursday")) {
                                        scheduleDays.add(Calendar.THURSDAY);
                                    } else if (elementText.equals("TuTh") || elementText.equals("TR")) {
                                        scheduleDays.add(Calendar.TUESDAY);
                                        scheduleDays.add(Calendar.THURSDAY);
                                    } else if (elementText.equals("Sa") || elementText.equals("Sat") || elementText.contains("Saturday")) {
                                        scheduleDays.add(Calendar.SATURDAY);
                                    } else if (elementText.equals("Su") || elementText.equals("Sun") || elementText.contains("Sunday")) {
                                        scheduleDays.add(Calendar.SUNDAY);
                                    }
                                }
                                if (!times.isEmpty()) {
                                    if (times.size() == 2) {
                                        events.add(new  Event("test", times.get(0), times.get(1)));
                                    } else if (times.size() == 1) {
                                        events.add(new Event("test", times.get(0), null));
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

        List<Integer> testList = new ArrayList<>();
        testList.add(Calendar.MONDAY);
        testList.add(Calendar.WEDNESDAY);
        testList.add(Calendar.FRIDAY);

        List<Integer> testTwo = new ArrayList<>();
        testTwo.add(Calendar.AM);
        testTwo.add(Calendar.AM);
        createEvent(testList, testTwo);
//        createEvent(Calendar.SUNDAY);

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

    private void createEvent(List<Integer> days, List<Integer> ampm) {
        Calendar now = Calendar.getInstance();
        int weekday = now.get(Calendar.DAY_OF_WEEK);
        if (weekday != days.get(0)) {
            int targetDay = (Calendar.SATURDAY - weekday + 7 - Math.abs(Calendar.SATURDAY - days.get(0))) % 7;
            now.add(Calendar.DAY_OF_YEAR, targetDay);
        }

        String byDay = "";
        for (int i = 0; i < days.size(); i++) {
            switch (days.get(i)) {
                case Calendar.SUNDAY:
                    byDay += "SU";
                    break;
                case Calendar.MONDAY:
                    byDay += "MO";
                    break;
                case Calendar.TUESDAY:
                    byDay += "TU";
                    break;
                case Calendar.WEDNESDAY:
                    byDay += "WE";
                    break;
                case Calendar.THURSDAY:
                    byDay += "TH";
                    break;
                case Calendar.FRIDAY:
                    byDay += "FR";
                    break;
                case Calendar.SATURDAY:
                    byDay += "SA";
                    break;
            }

            if (i != days.size() - 1) {
                byDay += ",";
            }
        }


        //Start time of event
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(Calendar.AM_PM, ampm.get(0));

        //End time of event
        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.AM_PM, ampm.get(1));

        endTime.set(2019, 1, 8, 8, 30);
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, now.getTimeInMillis())
//                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, "test")
                .putExtra(CalendarContract.Events.DESCRIPTION, "testing description");
        if (!byDay.isEmpty()) {
            intent.putExtra(CalendarContract.Events.RRULE, "FREQ=WEEKLY;BYDAY="+byDay);
        }
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