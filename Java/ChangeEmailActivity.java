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

public class ChangeEmailActivity extends AppCompatActivity {

    private static final String TAG = "ChangeEmail";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button msaveChangesBtn;
    private ProgressBar mProgressBar;
    private LinearLayout mchangeEmailForm;
    private boolean emailVerified = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

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
                    Intent intent = new Intent(ChangeEmailActivity.this, EmailVerificationActivity.class);
                    startActivity(intent);
                } else if(!emailVerified) {
                    Intent intent = new Intent(ChangeEmailActivity.this, EmailVerificationActivity.class);
                    startActivity(intent);
                } else{
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(ChangeEmailActivity.this, WelcomeActivity.class);
                    startActivity(intent);
                }
            }
        };

        mProgressBar = (ProgressBar) findViewById(R.id.changeEmailProgressBar);
        mProgressBar.setVisibility(View.GONE);

        mchangeEmailForm = (LinearLayout) findViewById(R.id.changeEmailForm);
        mchangeEmailForm.setVisibility(View.VISIBLE);

        mEmailField = (EditText) findViewById(R.id.changeEmailNewEmail);
        mPasswordField = (EditText) findViewById(R.id.changeEmailPassword);
        msaveChangesBtn = (Button) findViewById(R.id.changeEmailButton);


        msaveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                msaveChangesBtn.setEnabled(false);
                hideKeyboard();
                mchangeEmailForm.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);

                final String email = mEmailField.getText().toString().trim();
                final String password = mPasswordField.getText().toString().trim();

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (TextUtils.isEmpty(email)) {
                    mProgressBar.setVisibility(View.GONE);
                    mchangeEmailForm.setVisibility(View.VISIBLE);
                    msaveChangesBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Enter email!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    mProgressBar.setVisibility(View.GONE);
                    mchangeEmailForm.setVisibility(View.VISIBLE);
                    msaveChangesBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User re-authenticated.");
                            user.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User email address updated.");
                                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "Email sent!");
                                                    emailVerified = false;
                                                    mEmailField.setText("");
                                                    mPasswordField.setText("");
                                                    mProgressBar.setVisibility(View.GONE);
                                                    Toast.makeText(ChangeEmailActivity.this, "Email sent!", Toast.LENGTH_SHORT).show();
                                                    FirebaseAuth.getInstance().signOut();
                                                    mchangeEmailForm.setVisibility(View.VISIBLE);
                                                    msaveChangesBtn.setEnabled(true);
                                                }
                                                else {
                                                    Log.d(TAG, "Email failed sent!");
                                                    emailVerified = false;
                                                    mProgressBar.setVisibility(View.GONE);
                                                    Toast.makeText(getApplicationContext(), "Email verification failed to send!" + task.getException(), Toast.LENGTH_SHORT).show();
                                                    FirebaseAuth.getInstance().signOut();
                                                    mchangeEmailForm.setVisibility(View.VISIBLE);
                                                    msaveChangesBtn.setEnabled(true);
                                                }
                                            }
                                        });
                                    } else {
                                        Log.d(TAG, "User email address was not updated.");
                                        Toast.makeText(ChangeEmailActivity.this, "Failed to change email address!", Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                        mchangeEmailForm.setVisibility(View.VISIBLE);
                                        msaveChangesBtn.setEnabled(true);
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(ChangeEmailActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);
                            mchangeEmailForm.setVisibility(View.VISIBLE);
                            msaveChangesBtn.setEnabled(true);
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