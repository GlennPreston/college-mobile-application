package com.example.lastmanstanding;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateLeagueActivity extends AppCompatActivity {

    private static final String TAG = "CreateLeague";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    String UserID;
    private EditText mleagueName;
    private Button mcreateLeagueBtn;
    private ProgressBar mProgressBar;
    private LinearLayout mcreateLeagueForm;
    private TextView mcreateLeagueStartingGameweek;
    private TextView mcreateLeagueDeadline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_league);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    // User is signed in and email is verified
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    UserID=user.getUid();
                } else if(user != null && !user.isEmailVerified()) {
                    // User email is not verified
                    Intent intent = new Intent(CreateLeagueActivity.this, EmailVerificationActivity.class);
                    startActivity(intent);
                } else{
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(CreateLeagueActivity.this, WelcomeActivity.class);
                    startActivity(intent);
                }
            }
        };

        mProgressBar = (ProgressBar) findViewById(R.id.createLeagueProgressBar);
        mProgressBar.setVisibility(View.GONE);
        mcreateLeagueForm = (LinearLayout) findViewById(R.id.createLeagueForm);
        mcreateLeagueForm.setVisibility(View.VISIBLE);
        mcreateLeagueBtn = (Button) findViewById(R.id.createLeagueButton);
        mcreateLeagueBtn.setEnabled(true);
        mleagueName = (EditText)findViewById(R.id.createLeagueLeagueName);
        mcreateLeagueStartingGameweek = (TextView) findViewById(R.id.createLeagueStartingGameweek);
        mcreateLeagueDeadline = (TextView) findViewById(R.id.createLeagueDeadline);

        DatabaseReference gameweekRef= FirebaseDatabase.getInstance().getReference("Premier League").child("Current Gameweek");
        gameweekRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                DatabaseReference deadlineRef=FirebaseDatabase.getInstance().getReference("Premier League").child(dataSnapshot.getValue().toString()).child("Deadline");
                deadlineRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataChildSnapshot) {
                        mcreateLeagueStartingGameweek.setText("Week " + dataSnapshot.getValue().toString());
                        mcreateLeagueDeadline.setText(dataChildSnapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mcreateLeagueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mcreateLeagueBtn.setEnabled(false);
                hideKeyboard();
                mcreateLeagueForm.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                String leagueName = mleagueName.getText().toString().trim();

                if(TextUtils.isEmpty(leagueName)){
                    mProgressBar.setVisibility(View.GONE);
                    mcreateLeagueForm.setVisibility(View.VISIBLE);
                    mcreateLeagueBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Enter league name!", Toast.LENGTH_SHORT).show();
                    return;
                } else if(leagueName.length() > 20){
                    mProgressBar.setVisibility(View.GONE);
                    mcreateLeagueForm.setVisibility(View.VISIBLE);
                    mcreateLeagueBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Enter league name!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
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

    public void hideKeyboard() {
        try  {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
        }
    }
}
