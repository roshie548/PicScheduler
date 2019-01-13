package com.roshan.android.picscheduler;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

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
        events = new ArrayList<>();

//        initializeData();

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

                                int index = Integer.MAX_VALUE;

                                String title;

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
                                        if (lineText.indexOf(elementText) < index) {
                                            index = lineText.indexOf(elementText);
                                        }
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
                                            int amFirstIndex = -1;
                                            int amLastIndex = -1;
                                            int pmFirstIndex = -1;
                                            int pmLastIndex = -1;
                                            if (elementText.contains("A")) {
                                                amFirstIndex = elementText.indexOf("A");
                                                amLastIndex = elementText.lastIndexOf("A");
                                            }
                                            if (elementText.contains("P")) {
                                                pmFirstIndex = elementText.indexOf("P");
                                                pmLastIndex = elementText.lastIndexOf("P");
                                            }

                                            if (amFirstIndex != -1 && pmFirstIndex != -1) {
                                                if (amFirstIndex < pmFirstIndex) {
                                                    ampm.add(Calendar.AM);
                                                    ampm.add(Calendar.PM);
                                                } else {
                                                    ampm.add(Calendar.PM);
                                                    ampm.add(Calendar.AM);
                                                }
                                            } else if (amFirstIndex != -1) {
                                                if (amFirstIndex != amLastIndex) {
                                                    ampm.add(Calendar.AM);
                                                    ampm.add(Calendar.AM);
                                                }
                                            } else if (pmFirstIndex != -1) {
                                                if (pmFirstIndex != pmLastIndex) {
                                                    ampm.add(Calendar.PM);
                                                    ampm.add(Calendar.PM);
                                                }
                                            }
                                        } else {
                                            //Identifies AM and PM and add to the ampm ArrayList
                                            if (elementText.contains("AM") || (elementText.contains(":") && elementText.contains("A"))) {
                                                ampm.add(Calendar.AM);
                                            } else if (elementText.contains("PM") || (elementText.contains(":") && elementText.contains("P"))) {
                                                ampm.add(Calendar.PM);
                                            }
                                        }
                                    }



                                    //Identify which days and set it equal to the scheduleDays String
                                    //TBH this is ugly code :(
                                    if (elementText.equals("MWF") || elementText.equals("M,W,F")) {
                                        scheduleDays.add(Calendar.MONDAY);
                                        scheduleDays.add(Calendar.WEDNESDAY);
                                        scheduleDays.add(Calendar.FRIDAY);
                                        if (lineText.indexOf(elementText) < index) {
                                            index = lineText.indexOf(elementText);
                                        }
                                    } else if (elementText.equals("M") || elementText.equals("Mon") || elementText.contains("Monday")) {
                                        scheduleDays.add(Calendar.MONDAY);
                                        if (lineText.indexOf(elementText) < index) {
                                            index = lineText.indexOf(elementText);
                                        }
                                    } else if (elementText.equals("W") || elementText.equals("Wed") || elementText.contains("Wednesday")) {
                                        scheduleDays.add(Calendar.WEDNESDAY);
                                        if (lineText.indexOf(elementText) < index) {
                                            index = lineText.indexOf(elementText);
                                        }
                                    } else if (elementText.equals("F") || elementText.equals("Fri") || elementText.contains("Friday")) {
                                        scheduleDays.add(Calendar.FRIDAY);
                                        if (lineText.indexOf(elementText) < index) {
                                            index = lineText.indexOf(elementText);
                                        }
                                    } else if (elementText.equals("Tu") || elementText.equals("Tues") || elementText.contains("Tuesday")) {
                                        scheduleDays.add(Calendar.TUESDAY);
                                        if (lineText.indexOf(elementText) < index) {
                                            index = lineText.indexOf(elementText);
                                        }
                                    } else if (elementText.equals("Th") || elementText.equals("Thur") || elementText.equals("Thurs") || elementText.contains("Thursday")) {
                                        scheduleDays.add(Calendar.THURSDAY);
                                        if (lineText.indexOf(elementText) < index) {
                                            index = lineText.indexOf(elementText);
                                        }
                                    } else if (elementText.equals("TuTh") || elementText.equals("TR")) {
                                        scheduleDays.add(Calendar.TUESDAY);
                                        scheduleDays.add(Calendar.THURSDAY);
                                        if (lineText.indexOf(elementText) < index) {
                                            index = lineText.indexOf(elementText);
                                        }
                                    } else if (elementText.equals("Sa") || elementText.equals("Sat") || elementText.contains("Saturday")) {
                                        scheduleDays.add(Calendar.SATURDAY);
                                        if (lineText.indexOf(elementText) < index) {
                                            index = lineText.indexOf(elementText);
                                        }
                                    } else if (elementText.equals("Su") || elementText.equals("Sun") || elementText.contains("Sunday")) {
                                        scheduleDays.add(Calendar.SUNDAY);
                                        if (lineText.indexOf(elementText) < index) {
                                            index = lineText.indexOf(elementText);
                                        }
                                    }
                                }
                                if (index == Integer.MAX_VALUE || lineText.substring(0, index).isEmpty()) {
                                    title = "Event";
                                } else {
                                    title = lineText.substring(0, index);
                                }
                                if (!times.isEmpty()) {
                                    if (times.size() == 2) {
                                        events.add(new  Event(title, times.get(0), times.get(1), scheduleDays, ampm));
                                    } else if (times.size() == 1) {
                                        events.add(new Event(title, times.get(0), null, scheduleDays, ampm));
                                    } else {
                                        events.add(new Event(title, null, null, scheduleDays, ampm));
                                    }
                                }
                            }
                        }
                        RVAdapter adapter = new RVAdapter(ImageDetectActivity.this, events);
                        recyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    class Event {
        String name;
        String start;
        String end;
        List<Integer> days;
        int startHour;
        int startMinutes;
        int endHour = -1;
        int endMinutes = -1;
        List<Integer> ampm;

        //TODO: Optional parameters?
        Event(String name, String start, String end, List<Integer> days, List<Integer> ampm) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.days = days;
            this.ampm = ampm;

            if (start != null) {
                int i = start.indexOf(":");
                if (i != -1) {
                    try {
                        startHour = Integer.parseInt(start.substring(0, i));
                        startMinutes = Integer.parseInt(start.substring(i+1, start.length()));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Toast toast = Toast.makeText(getApplicationContext(), "There was an error recognizing the times. One or more of the events may be incorrect.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }

            if (end != null) {
                int j = end.indexOf(":");
                if (j != -1) {
                    try {
                        endHour = Integer.parseInt(end.substring(0, j));
                        endMinutes = Integer.parseInt(end.substring(j + 1, end.length()));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Toast toast =Toast.makeText(getApplicationContext(), "There was an error recognizing the times. One or more of the events may be incorrect.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }
        }
    }

}