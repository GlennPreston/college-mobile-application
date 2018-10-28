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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeNameActivity extends AppCompatActivity{

    private static final String TAG = "ChangeName";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText mFirstNameField;
    private EditText mLastNameField;
    private EditText mPasswordField;
    private Button msaveChangesBtn;
    private ProgressBar mProgressBar;
    private LinearLayout mchangeNameForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);

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
                    Intent intent = new Intent(ChangeNameActivity.this, EmailVerificationActivity.class);
                    startActivity(intent);
                } else{
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(ChangeNameActivity.this, WelcomeActivity.class);
                    startActivity(intent);
                }
            }
        };

        mProgressBar = (ProgressBar) findViewById(R.id.changeNameProgressBar);
        mProgressBar.setVisibility(View.GONE);

        mchangeNameForm = (LinearLayout) findViewById(R.id.changeNameForm);
        mchangeNameForm.setVisibility(View.VISIBLE);

        mFirstNameField = (EditText) findViewById(R.id.changeNameFirstName);
        mLastNameField = (EditText) findViewById(R.id.changeNameLastName);
        mPasswordField = (EditText) findViewById(R.id.changeNamePassword);
        msaveChangesBtn = (Button) findViewById(R.id.changeNameButton);


        msaveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msaveChangesBtn.setEnabled(false);
                hideKeyboard();
                mchangeNameForm.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);

                String firstName = mFirstNameField.getText().toString().trim();
                String lastName = mLastNameField.getText().toString().trim();
                final String password = mPasswordField.getText().toString().trim();

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (TextUtils.isEmpty(firstName)) {
                    mProgressBar.setVisibility(View.GONE);
                    mchangeNameForm.setVisibility(View.VISIBLE);
                    msaveChangesBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Enter first name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(lastName)) {
                    mProgressBar.setVisibility(View.GONE);
                    mchangeNameForm.setVisibility(View.VISIBLE);
                    msaveChangesBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Enter last name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mProgressBar.setVisibility(View.GONE);
                    mchangeNameForm.setVisibility(View.VISIBLE);
                    msaveChangesBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (firstName.length() > 15) {
                    mProgressBar.setVisibility(View.GONE);
                    mchangeNameForm.setVisibility(View.VISIBLE);
                    msaveChangesBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "First name too long, enter maximum 15 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (lastName.length() > 15) {
                    mProgressBar.setVisibility(View.GONE);
                    mchangeNameForm.setVisibility(View.VISIBLE);
                    msaveChangesBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Last name too long, enter maximum 15 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String name = firstName + " " + lastName;

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "User re-authenticated.");
                            String uid = user.getUid();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference userInfoRef = database.getReference("Users");
                            userInfoRef.child(uid).child("Name").setValue(name);
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User name updated.");
                                        mFirstNameField.setText("");
                                        mLastNameField.setText("");
                                        mPasswordField.setText("");
                                        mProgressBar.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(), "Name was successfully changed!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ChangeNameActivity.this, ProfileActivity.class));
                                        mchangeNameForm.setVisibility(View.VISIBLE);
                                        msaveChangesBtn.setEnabled(true);
                                    } else {
                                        Log.d(TAG, "User name was not updated.");
                                        mProgressBar.setVisibility(View.GONE);
                                        mchangeNameForm.setVisibility(View.VISIBLE);
                                        msaveChangesBtn.setEnabled(true);
                                        Toast.makeText(getApplicationContext(), "Failed to change name!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            mProgressBar.setVisibility(View.GONE);
                            msaveChangesBtn.setEnabled(true);
                            Toast.makeText(ChangeNameActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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