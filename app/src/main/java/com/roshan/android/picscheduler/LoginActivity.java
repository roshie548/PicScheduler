package com.roshan.android.picscheduler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.sign_in_button) SignInButton signInButton;

    public GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN;
    private static final String TAG = "Login Activity Error";
    GoogleSignInOptions gso;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences(MainActivity.MY_PREFERENCES, Context.MODE_PRIVATE);

        ButterKnife.bind(this);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope("https://www.googleapis.com/auth/calendar.events"))
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!sharedPreferences.getBoolean(MainActivity.SIGNED_IN, false)) {
            signOut();
        }
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            updateUI(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(MainActivity.SIGNED_IN, true);
            editor.apply();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.PERSON_NAME, account.getDisplayName());
            intent.putExtra(MainActivity.PERSON_FAMILY_NAME, account.getGivenName());
            intent.putExtra(MainActivity.PERSON_GIVEN_NAME, account.getFamilyName());
            intent.putExtra(MainActivity.PERSON_EMAIL, account.getEmail());
            intent.putExtra(MainActivity.PERSON_ID, account.getId());
            intent.putExtra(MainActivity.PERSON_PHOTO, account.getPhotoUrl());
            startActivity(intent);
        }
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

}
