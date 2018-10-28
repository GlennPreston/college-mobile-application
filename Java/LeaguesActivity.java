package com.example.lastmanstanding;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class LeaguesActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String admin,UserID;

    private static final String TAG = "Leagues";
    public static final String EXTRA_MESSAGE = "com.example.lastmanstanding.MESSAGE";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    private TextView mUser_name;
    private TextView mUser_email;
    private ListView mListView;
    private ProgressBar mProgressBar;
    private ArrayList<String> leaguesList = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(Bundle.EMPTY);
        setContentView(R.layout.activity_leagues);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //*Get User ID* START
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            admin = user.getEmail();
            UserID = user.getUid();
        }

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    // User is signed in
                    final String uid = user.getUid();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference userInfoRef = database.getReference("Users");

                    userInfoRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child(uid).child("Name").getValue() == null) {
                                FirebaseAuth.getInstance().signOut();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    View header = navigationView.getHeaderView(0);
                    mUser_name = (TextView) header.findViewById(R.id.drawerUserName);
                    mUser_email = (TextView) header.findViewById(R.id.drawerUserEmail);
                    mUser_name.setText(user.getDisplayName());
                    mUser_email.setText(user.getEmail());
                } else {
                    //User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(LeaguesActivity.this, WelcomeActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        };

        mProgressBar = (ProgressBar) findViewById(R.id.leaguesProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference usersLeagues = database.getReference("Users").child(UserID).child("Leagues");
        mListView = (ListView) findViewById(R.id.leaguesList);
        final ArrayAdapter<String> leaguesAdapter = new ArrayAdapter<String>(LeaguesActivity.this, android.R.layout.simple_list_item_1, leaguesList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextSize(24);
                textView.setPadding(60,60,60,60);
                return textView;
            }
        };
        mListView.setAdapter(leaguesAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                String leagueName = arg0.getItemAtPosition(position).toString();
                Intent intent = new Intent(LeaguesActivity.this, ViewLeagueActivity.class);
                intent.putExtra(EXTRA_MESSAGE, leagueName);
                startActivity(intent);
            }
        });

        usersLeagues.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                leaguesAdapter.clear();
                leaguesAdapter.notifyDataSetChanged();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    leaguesAdapter.add(child.getKey());
                    leaguesAdapter.notifyDataSetChanged();
                }
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.leagues, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_leagues) {
            onBackPressed();
        } else if (id == R.id.nav_fixtures_and_results) {
            Intent intent = new Intent(this, FixturesAndResultsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_how_to_play) {
            Intent intent = new Intent(this, HowToPlayActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    public void logOut (View v) {
        FirebaseAuth.getInstance().signOut();
    }

    public void createLeague(View view) {
        Intent intent = new Intent(this, CreateLeagueActivity.class);
        startActivity(intent);
    }

    public void joinLeague(View view) {
        Intent intent = new Intent(this, JoinLeagueActivity.class);
        startActivity(intent);
    }

    public void viewLeague(View view) {
        int leagueFieldId = view.getId();
        TextView leagueNameField = (TextView) findViewById(leagueFieldId);
        String leagueName = leagueNameField.getText().toString().trim();
        Intent intent = new Intent(this, ViewLeagueActivity.class);
        intent.putExtra(EXTRA_MESSAGE, leagueName);
        startActivity(intent);
    }
}