package com.example.lastmanstanding;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ViewLeagueActivity extends AppCompatActivity {

    private static final String TAG = "ViewLeague";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public static final String EXTRA_MESSAGE = "com.example.lastmanstanding.MESSAGE";

    private String league_name;
    private String admin;
    private ListView mListView;
    private ProgressBar mProgressBar;
    private ArrayList<String> participantsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_league);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    // User is signed in and email is verified
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    admin = user.getUid();
                } else if(user != null && !user.isEmailVerified()) {
                    // User email is not verified
                    Intent intent = new Intent(ViewLeagueActivity.this, EmailVerificationActivity.class);
                    startActivity(intent);
                } else{
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(ViewLeagueActivity.this, WelcomeActivity.class);
                    startActivity(intent);
                }
            }
        };

        Intent intent = getIntent();
        league_name = intent.getStringExtra(EXTRA_MESSAGE);

        TextView leagueName = (TextView) findViewById(R.id.viewLeagueLeagueName);
        leagueName.setText(league_name);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        final TextView deadlineField = (TextView) findViewById(R.id.viewLeagueDeadline);
        final TextView selectedTeamView = (TextView) findViewById(R.id.viewLeagueSelectedTeam);
        final ImageView teamBadgeView = (ImageView) findViewById(R.id.viewLeagueSelectedTeamBadge);
        final Button mpickTeamBtn = (Button) findViewById(R.id.viewLeagueButton);
        mpickTeamBtn.setVisibility(View.GONE);
        mProgressBar = (ProgressBar) findViewById(R.id.viewLeagueProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        DatabaseReference currentGameweekRef = database.getReference("Premier League").child("Current Gameweek");

        currentGameweekRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String currentGameweek = dataSnapshot.getValue().toString();
                DatabaseReference deadlineRef = database.getReference("Premier League").child(currentGameweek).child("Deadline");

                deadlineRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        deadlineField.setText(dataSnapshot.getValue().toString());
                        final Date currentTime = new Date();
                        final SimpleDateFormat sdf = new SimpleDateFormat("d MMM, HH:mm yyyy");
                        final TimeZone timezone = TimeZone.getTimeZone("Europe/Dublin");
                        timezone.useDaylightTime();
                        sdf.setTimeZone(timezone);
                        final Date deadline;

                        try {
                            if(Integer.parseInt(currentGameweek) <= 19) {
                                deadline = sdf.parse(dataSnapshot.getValue().toString() + " 2016");
                            } else {
                                deadline = sdf.parse(dataSnapshot.getValue().toString() + " 2017");
                            }

                            if(Boolean.toString(currentTime.before(deadline)).equals("true")) { // Changed to "true" for testing, change back to "false" for it to work properly
                                mpickTeamBtn.setVisibility(View.GONE);
                            } else{
                                DatabaseReference standingRef = database.getReference("Participants").child(league_name).child(user.getUid()).child("Standing");

                                standingRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getValue().toString().equals("In")) {
                                            mpickTeamBtn.setVisibility(View.VISIBLE);
                                        } else {
                                            mpickTeamBtn.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                            }
                        } catch (ParseException e) {
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                DatabaseReference selectedTeamRef = database.getReference("Participants").child(league_name).child(user.getUid()).child("Selected Team");

                selectedTeamRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null) {
                            teamBadgeView.setVisibility(View.VISIBLE);
                            selectedTeamView.setText(dataSnapshot.getValue().toString());
                            String teamName = dataSnapshot.getValue().toString().toLowerCase().replace(" ", "_") + "_badge";
                            int teamBadge = ViewLeagueActivity.this.getResources().getIdentifier(teamName, "drawable", ViewLeagueActivity.this.getPackageName());
                            teamBadgeView.setImageResource(teamBadge);
                        } else {
                            teamBadgeView.setVisibility(View.GONE);
                            selectedTeamView.setText("No team selected.");
                            teamBadgeView.setImageResource(0);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference leagueParticipants = database.getReference("Leagues").child(league_name).child("participants");
                mListView = (ListView) findViewById(R.id.viewLeagueList);
                final ArrayAdapter<String> participantsAdapter = new ArrayAdapter<String>(ViewLeagueActivity.this, android.R.layout.simple_list_item_1, participantsList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        final TextView textView = (TextView) super.getView(position, convertView, parent);
                        textView.setTextSize(24);
                        textView.setPadding(60,60,60,60);

                        DatabaseReference participantsRef = FirebaseDatabase.getInstance().getReference("Leagues").child(league_name).child("participants");
                        participantsRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                for (final DataSnapshot child : dataSnapshot.getChildren()) {
                                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(child.getValue().toString()).child("Name");
                                    userRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataChildSnapshot) {
                                            textView.setContentDescription(child.getValue().toString());
                                            textView.setText(dataChildSnapshot.getValue().toString());
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        return textView;
                    }
                };

                mListView.setAdapter(participantsAdapter);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
                    {
                        String participantsID = arg1.getContentDescription().toString();
                        Intent intent = new Intent(ViewLeagueActivity.this, ViewPicksActivity.class);
                        intent.putExtra(EXTRA_MESSAGE, participantsID);
                        startActivity(intent);
                    }
                });

                leagueParticipants.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        participantsAdapter.clear();
                        participantsAdapter.notifyDataSetChanged();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            participantsAdapter.add("");
                            participantsAdapter.notifyDataSetChanged();
                        }
                        mProgressBar.setVisibility(View.GONE);
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
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference adminRef = database.getReference("Leagues").child(league_name).child("admin");

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            admin = user.getUid();
        } else {
        }

        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(admin.equals(dataSnapshot.getValue())) {
                    getMenuInflater().inflate(R.menu.view_league_admin, menu);
                } else {
                    getMenuInflater().inflate(R.menu.view_league_participant, menu);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_manage_league) {
            Intent intent = new Intent(this, ManageLeagueActivity.class);
            startActivity(intent);
        } else if(id == R.id.action_leave_league) {
            Toast.makeText(getApplicationContext(), "You have left the league!", Toast.LENGTH_SHORT).show();
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

    public void pickTeam(View view) {
        TextView leagueNameField = (TextView) findViewById(R.id.viewLeagueLeagueName);
        String leagueName = leagueNameField.getText().toString().trim();
        Intent intent = new Intent(this, PickTeamActivity.class);
        intent.putExtra(EXTRA_MESSAGE, leagueName);
        startActivity(intent);
    }

    public void leagues(View view) {
        Intent intent = new Intent(this, LeaguesActivity.class);
        startActivity(intent);
    }
}