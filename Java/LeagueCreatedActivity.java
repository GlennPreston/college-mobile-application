package com.example.lastmanstanding;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LeagueCreatedActivity extends AppCompatActivity {

    private static final String TAG = "LeagueCreated";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public static final String EXTRA_MESSAGE = "com.example.lastmanstanding.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_created);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    // User is signed in and email is verified
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else if(user != null && !user.isEmailVerified()) {
                    // User email is not verified
                    Intent intent = new Intent(LeagueCreatedActivity.this, EmailVerificationActivity.class);
                    startActivity(intent);
                } else{
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(LeagueCreatedActivity.this, WelcomeActivity.class);
                    startActivity(intent);
                }
            }
        };

        Intent intent = getIntent();
        String leaguecode = intent.getStringExtra(EXTRA_MESSAGE);

        TextView leagueCode = (TextView) findViewById(R.id.leagueCreatedLeagueCode);
        leagueCode.setText(leaguecode);
    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void copyCode(View view) {
        TextView leagueCodeField = (TextView) findViewById(R.id.leagueCreatedLeagueCode);
        String leagueCode = leagueCodeField.getText().toString();
        ClipboardManager _clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText(leagueCode, leagueCode);
        _clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), "Code has been copied to clipboard!", Toast.LENGTH_SHORT).show();
    }

    public void leagues(View view) {
        Intent intent = new Intent(this, LeaguesActivity.class);
        startActivity(intent);
    }
}