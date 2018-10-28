package com.example.lastmanstanding;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ManageLeagueActivity extends AppCompatActivity {

    private static final String TAG = "ManageLeague";
    public static final String EXTRA_MESSAGE = "com.example.lastmanstanding.MESSAGE";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_league);

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
                    Intent intent = new Intent(ManageLeagueActivity.this, EmailVerificationActivity.class);
                    startActivity(intent);
                } else{
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(ManageLeagueActivity.this, WelcomeActivity.class);
                    startActivity(intent);
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_league, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_delete_league) {
            Toast.makeText(getApplicationContext(), "The league has been deleted!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LeaguesActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
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
        Toast.makeText(getApplicationContext(), "Code has been copied to clipboard!", Toast.LENGTH_SHORT).show();
    }

    public void removePlayer(View view) {
        Toast.makeText(getApplicationContext(), "Player has been removed!", Toast.LENGTH_SHORT).show();
    }
}