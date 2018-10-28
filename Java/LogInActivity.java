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

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = "Login";

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mloginBtn;
    private ProgressBar mProgressBar;
    private LinearLayout mlogInForm;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    // User is signed in and email is verified
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Intent intent = new Intent(LogInActivity.this, LeaguesActivity.class);
                    startActivity(intent);
                } else{
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        mProgressBar = (ProgressBar) findViewById(R.id.logInProgressBar);
        mProgressBar.setVisibility(View.GONE);

        mlogInForm = (LinearLayout) findViewById(R.id.logInForm);
        mlogInForm.setVisibility(View.VISIBLE);

        mEmailField = (EditText) findViewById(R.id.logInEmail);
        mPasswordField = (EditText) findViewById(R.id.logInPassword);
        mloginBtn = (Button) findViewById(R.id.logInButton);


        mloginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mloginBtn.setEnabled(false);
                hideKeyboard();
                mlogInForm.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);

                final String email = mEmailField.getText().toString().trim();
                final String password = mPasswordField.getText().toString().trim();


                if (TextUtils.isEmpty(email)) {
                    mProgressBar.setVisibility(View.GONE);
                    mlogInForm.setVisibility(View.VISIBLE);
                    mloginBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mProgressBar.setVisibility(View.GONE);
                    mlogInForm.setVisibility(View.VISIBLE);
                    mloginBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }



                //create user
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(LogInActivity.this, "signInWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    mProgressBar.setVisibility(View.GONE);
                                    mlogInForm.setVisibility(View.VISIBLE);
                                    mloginBtn.setEnabled(true);
                                    Toast.makeText(LogInActivity.this, "Authentication failed." + task.getException(), Toast.LENGTH_SHORT).show();
                                } else {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    boolean emailVerified = user.isEmailVerified();
                                    if(emailVerified) {
                                        mEmailField.setText("");
                                        mPasswordField.setText("");
                                        mProgressBar.setVisibility(View.GONE);
                                        startActivity(new Intent(LogInActivity.this, LeaguesActivity.class));
                                        mlogInForm.setVisibility(View.VISIBLE);
                                        mloginBtn.setEnabled(true);
                                        finish();
                                    }
                                    else {
                                        mProgressBar.setVisibility(View.GONE);
                                        mlogInForm.setVisibility(View.VISIBLE);
                                        mloginBtn.setEnabled(true);
                                        Toast.makeText(LogInActivity.this, "Email not verified!", Toast.LENGTH_SHORT).show();
                                    }
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

    public void signUp(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void passwordReset(View view) {
        Intent intent = new Intent(this, PasswordResetActivity.class);
        startActivity(intent);
    }
}