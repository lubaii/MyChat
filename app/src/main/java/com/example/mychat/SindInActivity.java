package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SindInActivity extends AppCompatActivity {

    private static final String TAG = "SignalInActiviti";
    private FirebaseAuth mAuth;

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText repeatpasswordEditText;
    private EditText namedEditText;
    private Button loginSignUpButton;
    private TextView toggleLoginSignUpTextView;

    private boolean loginModelActive = false;

    private FirebaseDatabase database;
    private DatabaseReference userDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sind_in);
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        userDatabaseReference = database.getReference().child("users");

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        repeatpasswordEditText = findViewById(R.id.repeatpasswordEditText);
        namedEditText = findViewById(R.id.namedEditText);
        toggleLoginSignUpTextView = findViewById(R.id.toggleLoginSignUpTextView);
        loginSignUpButton = findViewById(R.id.loginSignUpButton);

        loginSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginSignUpUser(emailEditText.getText().toString().trim(),
                        passwordEditText.getText().toString().trim());
            }
        });
        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(SindInActivity.this, UserListActivity.class)); // если пользователь зарегистрирован в чате
        }
    }

    private void loginSignUpUser(String email, String password) {
        if(loginModelActive){ // когда пользователь залогирован
            if(passwordEditText.getText().toString().length() < 7) {
                Toast.makeText(this,"Password must be at least 7 characters",Toast.LENGTH_SHORT).show();
            } else if (passwordEditText.getText().toString().trim().equals("")){
                Toast.makeText(this,"Please input you email",Toast.LENGTH_SHORT).show();
            } else {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) { // если пользователь залогинился
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent intent = new Intent(SindInActivity.this, UserListActivity.class);// присваиваем поле с именем
                                    intent.putExtra("userName", namedEditText.getText().toString().trim());
                                    startActivity(intent);
                                    //updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(SindInActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    //updateUI(null);
                                }
                            }
                        });
            }
        } else {
            if (!passwordEditText.getText().toString().trim().equals(repeatpasswordEditText.getText().toString().trim())) { // если пароли не совпадают
                Toast.makeText(this,"Password don't match",Toast.LENGTH_SHORT).show();
            } else if(passwordEditText.getText().toString().length() < 7) {
                Toast.makeText(this,"Password must be at least 7 characters",Toast.LENGTH_SHORT).show();
            } else if (passwordEditText.getText().toString().trim().equals("")){
                Toast.makeText(this,"Please input you email",Toast.LENGTH_SHORT).show();
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                createUser(user);
                                Intent intent = new Intent(SindInActivity.this, UserListActivity.class);// присваиваем поле с именем
                                intent.putExtra("userName",namedEditText.getText().toString().trim());
                                startActivity(intent);
                                //  updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SindInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                // updateUI(null);
                            }
                        }
                    });

        }

    }

    private void createUser(FirebaseUser firebaseUser ) {
        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setName(namedEditText.getText().toString().trim());

        userDatabaseReference.push().setValue(user);
    }

    public void toggleLoginMode(View view) {
        if(loginModelActive){ //==true
            loginModelActive = false;
            loginSignUpButton.setText("Signal Up");
            toggleLoginSignUpTextView.setText("Or, log in");
            repeatpasswordEditText.setVisibility(view.VISIBLE);

        } else {
            loginModelActive = true;
            loginSignUpButton.setText("Log in");
            toggleLoginSignUpTextView.setText("Or, sing up");
            repeatpasswordEditText.setVisibility(view.GONE);
        }
    }
}