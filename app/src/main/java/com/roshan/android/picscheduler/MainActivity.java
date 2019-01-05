package com.roshan.android.picscheduler;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.camerakit.CameraKitView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.camera) CameraKitView mCameraView;
    @BindView(R.id.camera_button) FloatingActionButton mCameraButton;
    @BindView(R.id.bar) BottomAppBar bottomAppBar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nav_view) NavigationView navigationView;

    private byte[] imageBytes;

    public static final String PERSON_NAME = "personName";
    public static final String PERSON_GIVEN_NAME = "personGivenName";
    public static final String PERSON_FAMILY_NAME = "personFamilyName";
    public static final String PERSON_EMAIL = "personEmail";
    public static final String PERSON_ID = "personId";
    public static final String PERSON_PHOTO = "personPhoto";
    public static final String MY_PREFERENCES = "MyPrefs";
    public static final String SIGNED_IN = "SignedIn";

    String personName;
    Uri personPhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);



        setSupportActionBar(bottomAppBar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        Intent intent = getIntent();
        personName = intent.getStringExtra(PERSON_NAME);
        personPhoto = intent.getParcelableExtra(PERSON_PHOTO);

        View headerView = navigationView.getHeaderView(0);
        TextView navName = headerView.findViewById(R.id.nav_name);
        CircleImageView navPicture = headerView.findViewById(R.id.nav_picture);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();

                        switch (menuItem.getItemId()) {
                            case R.id.sign_out:
                                signOut();
                                break;
                        }

                        return true;
                    }
                }
        );

        if (personName != null) {
            navName.setText(personName);
        }

        if (personPhoto != null) {
            Glide.with(this)
                    .load(personPhoto)
                    .into(navPicture);
        }

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
                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                    }
                });

                //TODO: Launch new activity, pass the bitmap
                // Maybe use Fragments instead of new activity?
            }
        });
    }

    private void signOut() {
        SharedPreferences sharedPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SIGNED_IN, false);
        editor.commit();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
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

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
