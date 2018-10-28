package com.example.lastmanstanding;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PickTeamActivity extends AppCompatActivity {

    private static final String TAG = "PickTeam";
    public static final String EXTRA_MESSAGE = "com.example.lastmanstanding.MESSAGE";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String team;
    private String leagueName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_team);

        Intent intent = getIntent();
        leagueName = intent.getStringExtra(EXTRA_MESSAGE);

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
                    Intent intent = new Intent(PickTeamActivity.this, EmailVerificationActivity.class);
                    startActivity(intent);
                } else{
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(PickTeamActivity.this, WelcomeActivity.class);
                    startActivity(intent);
                }
            }
        };

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference participantsRef = database.getReference("Participants").child(leagueName).child(user.getUid()).child("Available");

        participantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ((ViewGroup) findViewById(R.id.pickTeamRadioGroup)).removeAllViews();
                for(int row = 0; row<1; row++) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        RadioButton rdbtn = new RadioButton(PickTeamActivity.this);
                        Drawable teamBadgeDrawable;
                        rdbtn.setText(child.getKey());
                        rdbtn.setTextSize(24);
                        rdbtn.setPadding(8, 24, 0, 24);
                        rdbtn.setCompoundDrawablePadding(24);
                        rdbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int teamid = v.getId();
                                TextView teamName = (TextView) findViewById(teamid);
                                team = teamName.getText().toString();
                            }
                        });
                        String teamName = child.getKey().toLowerCase().replace(" ", "_") + "_badge";
                        int teamBadge = PickTeamActivity.this.getResources().getIdentifier(teamName, "drawable", PickTeamActivity.this.getPackageName());
                        teamBadgeDrawable = ContextCompat.getDrawable(PickTeamActivity.this, teamBadge);
                        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
                        Bitmap bitmap = ((BitmapDrawable) teamBadgeDrawable).getBitmap();
                        Drawable scaledDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, px, px, true));
                        rdbtn.setCompoundDrawablesWithIntrinsicBounds(scaledDrawable,null,null,null);
                        ((ViewGroup) findViewById(R.id.pickTeamRadioGroup)).addView(rdbtn);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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

    public void selectTeam(View view) {
        final FirebaseUser user = mAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference selectedTeamRef = database.getReference("Participants").child(leagueName).child(user.getUid()).child("Selected Team");
        selectedTeamRef.setValue(team);
        Intent intent = new Intent(this, ViewLeagueActivity.class);
        intent.putExtra(EXTRA_MESSAGE, leagueName);
        startActivity(intent);
    }
}