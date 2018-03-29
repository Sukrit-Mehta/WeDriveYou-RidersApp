package com.example.sukrit.ridersappwedriveyou.Views.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sukrit.ridersappwedriveyou.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    TextView tvSignUp,tvForgotPwd;
    TextInputLayout mLoginEmail,mLoginPassword;
    Button mLoginBtn;
    ProgressDialog mLoginProgress;
    DatabaseReference mUserDatabase;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();

        mUserDatabase.keepSynced(true);

        tvSignUp = findViewById(R.id.signUp);
        tvForgotPwd = findViewById(R.id.forgotPwd);

        mLoginProgress = new ProgressDialog(this);

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                finish();
            }
        });

        mLoginEmail= findViewById(R.id.login_email);
        mLoginPassword = findViewById(R.id.login_password);
        mLoginBtn= findViewById(R.id.login_btn);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=mLoginEmail.getEditText().getText().toString();
                String password=mLoginPassword.getEditText().getText().toString();
                if((!TextUtils.isEmpty(email))||(!TextUtils.isEmpty(password)))
                {
                    mLoginProgress.setTitle("Logging In...");
                    mLoginProgress.setMessage("Please wait while we check your credentials...");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();
                    loginUser(email,password);
                }
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                            mLoginProgress.dismiss();
                            Toast.makeText(LoginActivity.this, "Sign IN Successful.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            mLoginProgress.dismiss();
                            Toast.makeText(LoginActivity.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
