package com.example.lastmanstanding;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "Signup";

    private EditText mFirstNameField;
    private EditText mLastNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mConfirmPasswordField;
    private Button msignupBtn;
    private ProgressBar mProgressBar;
    private LinearLayout msignUpForm;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mProgressBar = (ProgressBar) findViewById(R.id.signUpProgressBar);
        mProgressBar.setVisibility(View.GONE);

        msignUpForm = (LinearLayout) findViewById(R.id.signUpForm);
        msignUpForm.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();

        mFirstNameField = (EditText) findViewById(R.id.signUpFirstName);
        mLastNameField = (EditText) findViewById(R.id.signUpLastName);
        mEmailField = (EditText) findViewById(R.id.signUpEmail);
        mPasswordField = (EditText) findViewById(R.id.signUpPassword);
        mConfirmPasswordField = (EditText) findViewById(R.id.signUpConfirmPassword);
        msignupBtn = (Button) findViewById(R.id.signUpButton);


        msignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                msignupBtn.setEnabled(false);
                hideKeyboard();
                msignUpForm.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);

                final String firstName = mFirstNameField.getText().toString().trim();
                final String lastName = mLastNameField.getText().toString().trim();
                final String email = mEmailField.getText().toString().trim();
                final String password = mPasswordField.getText().toString().trim();
                final String confirmPassword = mConfirmPasswordField.getText().toString().trim();



                if (TextUtils.isEmpty(firstName)) {
                    mProgressBar.setVisibility(View.GONE);
                    msignUpForm.setVisibility(View.VISIBLE);
                    msignupBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Enter first name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(lastName)) {
                    mProgressBar.setVisibility(View.GONE);
                    msignUpForm.setVisibility(View.VISIBLE);
                    msignupBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Enter last name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    mProgressBar.setVisibility(View.GONE);
                    msignUpForm.setVisibility(View.VISIBLE);
                    msignupBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mProgressBar.setVisibility(View.GONE);
                    msignUpForm.setVisibility(View.VISIBLE);
                    msignupBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(confirmPassword)) {
                    mProgressBar.setVisibility(View.GONE);
                    msignUpForm.setVisibility(View.VISIBLE);
                    msignupBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Confirm your password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (firstName.length() > 15) {
                    mProgressBar.setVisibility(View.GONE);
                    msignUpForm.setVisibility(View.VISIBLE);
                    msignupBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "First name too long, enter maximum 15 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (lastName.length() > 15) {
                    mProgressBar.setVisibility(View.GONE);
                    msignUpForm.setVisibility(View.VISIBLE);
                    msignupBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Last name too long, enter maximum 15 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    mProgressBar.setVisibility(View.GONE);
                    msignUpForm.setVisibility(View.VISIBLE);
                    msignupBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    mProgressBar.setVisibility(View.GONE);
                    msignUpForm.setVisibility(View.VISIBLE);
                    msignupBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Passwords don't match!", Toast.LENGTH_SHORT).show();
                    return;
                }



                //create user
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(SignUpActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    mProgressBar.setVisibility(View.GONE);
                                    msignUpForm.setVisibility(View.VISIBLE);
                                    msignupBtn.setEnabled(true);
                                    Toast.makeText(SignUpActivity.this, "Authentication failed." + task.getException(), Toast.LENGTH_SHORT).show();
                                } else {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                    if (user != null) {
                                        // User is signed in
                                        user.sendEmailVerification()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "Email sent.");
                                                            Toast.makeText(SignUpActivity.this, "Email sent.", Toast.LENGTH_SHORT).show();
                                                        }
                                                        else {
                                                            Toast.makeText(SignUpActivity.this, "Email failed to send." + task.getException(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                        // Writes name to database
                                        String name = firstName + " " + lastName;
                                        String uid = user.getUid();
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference userInfoRef = database.getReference("Users");
                                        userInfoRef.child(uid).child("Name").setValue(name);
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                                        user.updateProfile(profileUpdates);
                                        mFirstNameField.setText("");
                                        mLastNameField.setText("");
                                        mEmailField.setText("");
                                        mPasswordField.setText("");
                                        mConfirmPasswordField.setText("");
                                        mProgressBar.setVisibility(View.GONE);
                                        startActivity(new Intent(SignUpActivity.this, EmailVerificationActivity.class));
                                        msignUpForm.setVisibility(View.VISIBLE);
                                        msignupBtn.setEnabled(true);
                                    } else {
                                        // No user is signed in
                                    }
                                }
                            }
                        });
            }
        });
    }

    public void hideKeyboard() {
        try  {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
        }
    }

    public void logIn(View view) {
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
    }

}